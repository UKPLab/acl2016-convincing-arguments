/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Argument;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.graph.ArgumentPairListSorter;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.graph.AscendingScoreArgumentPairListSorter;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.graph.DescendingScoreArgumentPairListSorter;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.graph.RandomArgumentPairListSorter;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.algorithm.BellmanFord;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step6GraphTransitivityCleaner
{
    private static final String EDGE_PRIOR_WEIGHT = "edgePriorWeight";

    private static final Sigmoid SIGMOID = new Sigmoid();

    private static final String WEIGHT = "weight";

    private static final String LABEL_EQUAL = "equal";

    /**
     * Node's attribute for storing a set of all arguments in this particular node (could be
     * one for a "normal" node of multiple for a "clustered" node)
     */
    public static final String NODE_ATTR_CLUSTERED_ARGS = "attrClusteredArgs";

    /**
     * Lambda penalty for disagreeing labels in argument pair
     */
    public static final double LAMBDA_PENALTY = 10.0;

    private final ArgumentPairListSorter argumentPairListSorter;
    private final boolean removeEqualEdgesParam;

    public Step6GraphTransitivityCleaner(ArgumentPairListSorter argumentPairListSorter,
            boolean removeEqualEdgesParam)
    {
        this.argumentPairListSorter = argumentPairListSorter;
        this.removeEqualEdgesParam = removeEqualEdgesParam;
    }

    public GraphCleaningResults processSingleFile(File file, File outputDir, String prefix,
            Boolean collectGeneratedArgumentPairs)
            throws Exception
    {
        GraphCleaningResults result = new GraphCleaningResults();

        File outFileTable = new File(outputDir, prefix + file.getName() + "_table.csv");
        File outFileInfo = new File(outputDir, prefix + file.getName() + "_info.txt");

        PrintStream psTable = new PrintStream(new FileOutputStream(outFileTable));
        PrintStream psInfo = new PrintStream(new FileOutputStream(outFileInfo));

        // load one topic/side
        List<AnnotatedArgumentPair> pairs = new ArrayList<>(
                (List<AnnotatedArgumentPair>) XStreamTools.getXStream().fromXML(file));

        int fullDataSize = pairs.size();

        // filter out missing gold data
        Iterator<AnnotatedArgumentPair> iterator = pairs.iterator();
        while (iterator.hasNext()) {
            AnnotatedArgumentPair pair = iterator.next();
            if (pair.getGoldLabel() == null) {
                iterator.remove();
            }
            // or we want to completely remove equal edges in advance!
            else if (this.removeEqualEdgesParam && "equal".equals(pair.getGoldLabel())) {
                iterator.remove();
            }
        }

        // sort pairs by their weight
        this.argumentPairListSorter.sortArgumentPairs(pairs);

        int preFilteredDataSize = pairs.size();

        // compute correlation between score threshold and number of removed edges
        double[] correlationEdgeWeights = new double[pairs.size()];
        double[] correlationRemovedEdges = new double[pairs.size()];

        // only cycles of length 0 to 5 are interesting (5+ are too big)
        Range<Integer> range = Range.between(0, 5);

        psTable.print(
                "EdgeWeightThreshold\tPairs\tignoredEdgesCount\tIsDAG\tTransitivityScoreMean\tTransitivityScoreMax\tTransitivityScoreSamples\tEdges\tNodes\t");
        for (int j = range.getMinimum(); j <= range.getMaximum(); j++) {
            psTable.print("Cycles_" + j + "\t");
        }
        psTable.println();

        // store the indices of all pairs (edges) that have been successfully added without
        // generating cycles
        TreeSet<Integer> addedPairsIndices = new TreeSet<>();

        // number of edges ignored as they generated cycles
        int ignoredEdgesCount = 0;

        Graph lastGraph = null;

        // flag that the first cycle was already processed
        boolean firstCycleAlreadyHit = false;

        for (int i = 1; i < pairs.size(); i++) {
            // now filter the finalArgumentPairList and add only pairs that have not generated cycles
            List<AnnotatedArgumentPair> subList = new ArrayList<>();

            for (Integer index : addedPairsIndices) {
                subList.add(pairs.get(index));
            }

            // and add the current at the end
            subList.add(pairs.get(i));

            // what is the current lowest value of a pair weight?
            double weakestEdgeWeight = computeEdgeWeight(subList.get(subList.size() - 1),
                    LAMBDA_PENALTY);

            //            Graph graph = buildGraphFromArgumentPairs(finalArgumentPairList);
            int numberOfLoops;

            // map for storing cycles by their length
            TreeMap<Integer, TreeSet<String>> lengthCyclesMap = new TreeMap<>();

            Graph graph = buildGraphFromArgumentPairs(subList);

            lastGraph = graph;

            List<List<Object>> cyclesInGraph = findCyclesInGraph(graph);

            DescriptiveStatistics transitivityScore = new DescriptiveStatistics();

            if (cyclesInGraph.isEmpty()) {
                // we have DAG
                transitivityScore = computeTransitivityScores(graph);

                // update results
                result.maxTransitivityScore = (int) transitivityScore.getMax();
                result.avgTransitivityScore = transitivityScore.getMean();
            }

            numberOfLoops = cyclesInGraph.size();

            // initialize map
            for (int r = range.getMinimum(); r <= range.getMaximum(); r++) {
                lengthCyclesMap.put(r, new TreeSet<String>());
            }

            // we hit a loop
            if (numberOfLoops > 0) {
                // let's update the result

                if (!firstCycleAlreadyHit) {
                    result.graphSizeEdgesBeforeFirstCycle = graph.getEdgeCount();
                    result.graphSizeNodesBeforeFirstCycle = graph.getNodeCount();

                    // find the shortest cycle
                    int shortestCycleLength = Integer.MAX_VALUE;

                    for (List<Object> cycle : cyclesInGraph) {
                        shortestCycleLength = Math.min(shortestCycleLength, cycle.size());
                    }
                    result.lengthOfFirstCircle = shortestCycleLength;

                    result.pairsBeforeFirstCycle = i;

                    firstCycleAlreadyHit = true;
                }

                // ignore this edge further
                ignoredEdgesCount++;

                // update counts of different cycles lengths
                for (List<Object> cycle : cyclesInGraph) {
                    int currentSize = cycle.size();

                    // convert to sorted set of nodes
                    List<String> cycleAsSortedIDs = new ArrayList<>();
                    for (Object o : cycle) {
                        cycleAsSortedIDs.add(o.toString());
                    }
                    Collections.sort(cycleAsSortedIDs);

                    if (range.contains(currentSize)) {
                        lengthCyclesMap.get(currentSize).add(cycleAsSortedIDs.toString());
                    }
                }
            }
            else {
                addedPairsIndices.add(i);
            }

            // we hit the first cycle

            // collect loop sizes
            StringBuilder loopsAsString = new StringBuilder();
            for (int j = range.getMinimum(); j <= range.getMaximum(); j++) {
                //                    loopsAsString.append(j).append(":");
                loopsAsString.append(lengthCyclesMap.get(j).size());
                loopsAsString.append("\t");
            }

            psTable.printf(Locale.ENGLISH, "%.4f\t%d\t%d\t%b\t%.2f\t%d\t%d\t%d\t%d\t%s%n",
                    weakestEdgeWeight, i, ignoredEdgesCount, numberOfLoops == 0,
                    Double.isNaN(transitivityScore.getMean()) ? 0d : transitivityScore.getMean(),
                    (int) transitivityScore.getMax(), transitivityScore.getN(),
                    graph.getEdgeCount(), graph.getNodeCount(), loopsAsString.toString().trim());

            // update result
            result.finalGraphSizeEdges = graph.getEdgeCount();
            result.finalGraphSizeNodes = graph.getNodeCount();
            result.ignoredEdgesThatBrokeDAG = ignoredEdgesCount;

            // update stats for correlation
            correlationEdgeWeights[i] = weakestEdgeWeight;
            //            correlationRemovedEdges[i] =  (double) ignoredEdgesCount;
            // let's try: if we keep = 0, if we remove = 1
            correlationRemovedEdges[i] = numberOfLoops == 0 ? 0.0 : 1.0;
        }

        psInfo.println("Original: " + fullDataSize + ", removed by MACE: " + (fullDataSize
                - preFilteredDataSize) + ", final: " + (preFilteredDataSize - ignoredEdgesCount)
                + " (removed: " + ignoredEdgesCount + ")");

        double[][] matrix = new double[correlationEdgeWeights.length][];
        for (int i = 0; i < correlationEdgeWeights.length; i++) {
            matrix[i] = new double[2];
            matrix[i][0] = correlationEdgeWeights[i];
            matrix[i][1] = correlationRemovedEdges[i];
        }

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(matrix);

        double pValue = pearsonsCorrelation.getCorrelationPValues().getEntry(0, 1);
        double correlation = pearsonsCorrelation.getCorrelationMatrix().getEntry(0, 1);

        psInfo.printf(Locale.ENGLISH, "Correlation: %.3f, p-Value: %.4f%n", correlation, pValue);
        if (lastGraph == null) {
            throw new IllegalStateException("Graph is null");
        }

        // close
        psInfo.close();
        psTable.close();

        // save filtered final gold data
        List<AnnotatedArgumentPair> finalArgumentPairList = new ArrayList<>();

        for (Integer index : addedPairsIndices) {
            finalArgumentPairList.add(pairs.get(index));
        }
        XStreamTools.toXML(finalArgumentPairList, new File(outputDir, prefix + file.getName()));

        // TODO: here, we can add newly generated edges from graph transitivity
        if (collectGeneratedArgumentPairs) {
            Set<GeneratedArgumentPair> generatedArgumentPairs = new HashSet<>();
            // collect all arguments
            Map<String, Argument> allArguments = new HashMap<>();
            for (ArgumentPair argumentPair : pairs) {
                allArguments.put(argumentPair.getArg1().getId(), argumentPair.getArg1());
                allArguments.put(argumentPair.getArg2().getId(), argumentPair.getArg2());
            }

            Graph finalGraph = buildGraphFromArgumentPairs(finalArgumentPairList);
            for (Edge e : finalGraph.getEdgeSet()) {
                e.setAttribute(WEIGHT, 1.0);
            }

            for (Node j : finalGraph) {
                for (Node k : finalGraph) {
                    if (j != k) {
                        // is there a path between?
                        BellmanFord bfShortest = new BellmanFord(WEIGHT, j.getId());
                        bfShortest.init(finalGraph);
                        bfShortest.compute();

                        Path shortestPath = bfShortest.getShortestPath(k);

                        if (shortestPath.size() > 0) {
                            // we have a path
                            GeneratedArgumentPair ap = new GeneratedArgumentPair();
                            Argument arg1 = allArguments.get(j.getId());

                            if (arg1 == null) {
                                throw new IllegalStateException(
                                        "Cannot find argument " + j.getId());
                            }
                            ap.setArg1(arg1);

                            Argument arg2 = allArguments.get(k.getId());

                            if (arg2 == null) {
                                throw new IllegalStateException(
                                        "Cannot find argument " + k.getId());
                            }
                            ap.setArg2(arg2);

                            ap.setGoldLabel("a1");
                            generatedArgumentPairs.add(ap);
                        }
                    }
                }
            }
            // and now add the reverse ones
            Set<GeneratedArgumentPair> generatedReversePairs = new HashSet<>();
            for (GeneratedArgumentPair pair : generatedArgumentPairs) {
                GeneratedArgumentPair ap = new GeneratedArgumentPair();
                ap.setArg1(pair.getArg2());
                ap.setArg2(pair.getArg1());
                ap.setGoldLabel("a2");
                generatedReversePairs.add(ap);
            }
            generatedArgumentPairs.addAll(generatedReversePairs);
            // and save it
            XStreamTools.toXML(generatedArgumentPairs,
                    new File(outputDir, "generated_" + prefix + file.getName()));
        }

        result.fullPairsSize = fullDataSize;
        result.removedApriori = (fullDataSize - preFilteredDataSize);
        result.finalPairsRetained = finalArgumentPairList.size();

        // save the final graph
        Graph outGraph = cleanCopyGraph(lastGraph);
        FileSinkDGS dgs1 = new FileSinkDGS();
        File outFile = new File(outputDir, prefix + file.getName() + ".dgs");

        System.out.println("Saved to " + outFile);
        FileWriter w1 = new FileWriter(outFile);

        dgs1.writeAll(outGraph, w1);
        w1.close();

        return result;
    }

    public static class GraphCleaningResults
    {
        // pairs added before first cycle detected (average + stdev)
        public int pairsBeforeFirstCycle;

        // length of first cycle (average + stdev)
        public int lengthOfFirstCircle;

        // graphs size (nodes) before first cycle (average + stddev)
        public int graphSizeNodesBeforeFirstCycle;

        // graph size (edges) before first cycle (avg + stdev)
        public int graphSizeEdgesBeforeFirstCycle;

        // ignored edges that broke DAG at the end (avg + stdev)
        public int ignoredEdgesThatBrokeDAG;

        // graphs size (nodes) at the end (average + stddev)
        public int finalGraphSizeNodes;

        // graph size (edges) at the end (avg + stdev)
        public int finalGraphSizeEdges;

        // avg. transitivity score (avg + stdev)
        public double avgTransitivityScore;

        // max transitivity score (avg + stdev)
        public int maxTransitivityScore;

        // starting position
        public int fullPairsSize;

        // starting position
        public int removedApriori;

        // how many is left (directed + equal?)
        public int finalPairsRetained;

        @Override
        public String toString()
        {
            return "GraphCleaningResults{" +
                    "pairsBeforeFirstCycle=" + pairsBeforeFirstCycle +
                    ", lengthOfFirstCircle=" + lengthOfFirstCircle +
                    ", graphSizeNodesBeforeFirstCycle=" + graphSizeNodesBeforeFirstCycle +
                    ", graphSizeEdgesBeforeFirstCycle=" + graphSizeEdgesBeforeFirstCycle +
                    ", ignoredEdgesThatBrokeDAG=" + ignoredEdgesThatBrokeDAG +
                    ", finalGraphSizeNodes=" + finalGraphSizeNodes +
                    ", finalGraphSizeEdges=" + finalGraphSizeEdges +
                    ", avgTransitivityScore=" + avgTransitivityScore +
                    ", maxTransitivityScore=" + maxTransitivityScore +
                    ", fullPairsSize=" + fullPairsSize +
                    ", removedApriori=" + removedApriori +
                    ", finalPairsRetained=" + finalPairsRetained +
                    '}';
        }

    }

    /**
     * Creates a "clean" copy of the source graph with the same nodes+edges and cluster names;
     * the rest is omitted.
     *
     * @param graph graph
     * @return graph
     */
    public static Graph cleanCopyGraph(Graph graph)
    {
        Graph result = new DefaultGraph("g", true, false);

        for (Node n : graph) {
            Node node = result.addNode(n.getId());
            node.setAttribute(NODE_ATTR_CLUSTERED_ARGS, n.getAttribute(NODE_ATTR_CLUSTERED_ARGS));
        }

        for (Edge e : graph.getEdgeSet()) {
            result.addEdge(e.getId(), e.getSourceNode().getId(), e.getTargetNode().getId(), true);
        }

        return result;
    }

    /**
     * Transitivity score is a collection of scores computed for each pair of nodes A and B with
     * multiple paths between them. A single score for such a pair is a ratio of the longest
     * path from A to B and the shortest path.
     * <p/>
     * It uses Bellman-Ford algorithm to compute the shortest and longest paths
     *
     * @param graph graph (must be DAG)
     * @return statistics
     */
    private static DescriptiveStatistics computeTransitivityScores(Graph graph)
    {
        // find all out-degree > 1 nodes
        Set<Node> sourceNodes = new HashSet<>();
        Set<Node> targetNodes = new HashSet<>();
        for (Node n : graph) {
            if (n.getOutDegree() > 1) {
                sourceNodes.add(n);
            }

            if (n.getInDegree() > 1) {
                targetNodes.add(n);
            }
        }

        FileSourceDGS source = new FileSourceDGS();
        source.addSink(graph);

        DescriptiveStatistics result = new DescriptiveStatistics();

        for (Node sourceNode : sourceNodes) {
            // set positive weight first
            for (Edge e : graph.getEdgeSet()) {
                e.setAttribute(WEIGHT, 1.0);
            }

            BellmanFord bfShortest = new BellmanFord(WEIGHT, sourceNode.getId());
            bfShortest.init(graph);
            bfShortest.compute();

            // now negative weight for longest-path
            for (Edge e : graph.getEdgeSet()) {
                e.setAttribute(WEIGHT, -1.0);
            }

            BellmanFord bfLongest = new BellmanFord(WEIGHT, sourceNode.getId());
            bfLongest.init(graph);
            bfLongest.compute();

            for (Node targetNode : targetNodes) {
                Path shortestPath = bfShortest.getShortestPath(targetNode);
                Path longestPath = bfLongest.getShortestPath(targetNode);

                int shortestPathLength = shortestPath.getEdgeCount();
                int longestPathLength = longestPath.getEdgeCount();

                if (shortestPathLength == 1 && longestPathLength > 1) {
                    // update statistics
                    result.addValue((double) longestPathLength / (double) shortestPathLength);
                }
            }
        }

        return result;
    }

    /**
     * Sorts a list of argument pairs (by edge weight) descending
     *
     * @param list list
     */
    /*
    public static void sortArgumentPairs(List<AnnotatedArgumentPair> list)
    {
        Collections.sort(list, new Comparator<AnnotatedArgumentPair>()
        {
            @Override
            public int compare(AnnotatedArgumentPair o1, AnnotatedArgumentPair o2)
            {
                //                return Double.compare(computeEdgeWeight(o2, LAMBDA_PENALTY),
                //                        computeEdgeWeight(o1, LAMBDA_PENALTY));
                return Double.compare(computeEdgeWeight(o1, LAMBDA_PENALTY),
                        computeEdgeWeight(o2, LAMBDA_PENALTY));
            }
        });
    }
    */

    /**
     * Find all elementary cycles in graph (with hard limit 10k cycles to prevent overflow)
     *
     * @param graph graph
     * @return list of paths (path = list of node ID) of null, if self-loop found
     */
    private static List<List<Object>> findCyclesInGraph(Graph graph)
    {
        // convert into adjacency matrix representation
        SortedMap<String, Integer> nodeToIndexMap = new TreeMap<>();

        ArrayList<Node> origNodes = new ArrayList<>(graph.getNodeSet());
        for (int i = 0; i < origNodes.size(); i++) {
            nodeToIndexMap.put(origNodes.get(i).getId(), i);
        }

        // convert to different format for the algorithm
        String[] nodeIds = new String[origNodes.size()];
        boolean adjMatrix[][] = new boolean[origNodes.size()][origNodes.size()];

        // fill node IDs to array
        for (Map.Entry<String, Integer> entry : nodeToIndexMap.entrySet()) {
            nodeIds[entry.getValue()] = entry.getKey();
        }

        // fill adjacency matrix
        for (Edge edge : graph.getEdgeSet()) {
            String sourceId = edge.getSourceNode().getId();
            String targetId = edge.getTargetNode().getId();

            int sourceIndex = nodeToIndexMap.get(sourceId);
            int targetIndex = nodeToIndexMap.get(targetId);

            adjMatrix[sourceIndex][targetIndex] = true;
        }

        // let's do the magic :)
        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, nodeIds);

        List<List<Object>> cycles = ecs.getElementaryCycles();

        // since the algorithm doesn't reveal self-loops, find them by ourselves
        for (Edge edge : graph.getEdgeSet()) {
            if (edge.getTargetNode().getId().equals(edge.getSourceNode().getId())) {
                //                cycles.add(Arrays.asList((Object) edge.getSourceNode().getId(),
                //                        edge.getTargetNode().getId()));
                cycles.add(Collections.<Object>singletonList(edge.getSourceNode().getId()));
            }
        }

        //        System.out.println(cycles);

        return cycles;
    }

    protected static Set<Set<String>> buildEquivalencyClusters(
            List<AnnotatedArgumentPair> argumentPairs)
    {
        // create a new undirected graph
        Graph ccGraph = new DefaultGraph("ConnectedComponentsGraph");
        ccGraph.setAutoCreate(true);
        ccGraph.setStrict(false);

        for (AnnotatedArgumentPair annotatedArgumentPair : argumentPairs) {
            String goldLabel = annotatedArgumentPair.getGoldLabel();
            if (goldLabel == null) {
                throw new IllegalStateException(
                        "No gold label for pair " + annotatedArgumentPair.getId());
            }

            if (LABEL_EQUAL.equals(goldLabel)) {
                String arg1 = annotatedArgumentPair.getArg1().getId();
                String arg2 = annotatedArgumentPair.getArg2().getId();

                List<String> names = Arrays.asList(arg1, arg2);
                Collections.sort(names);
                String edgeName = StringUtils.join(names, "_");
                ccGraph.addEdge(edgeName, arg1, arg2);
            }
        }
        // compute connected components
        ConnectedComponents cc = new ConnectedComponents();
        cc.init(ccGraph);

        cc.setCountAttribute("cluster");
        cc.compute();

        //        System.out.println(cc.getConnectedComponentsCount());

        Set<Set<String>> equalClusters = new HashSet<>();

        // re-create clusters from all connected components
        for (ConnectedComponents.ConnectedComponent component : cc) {
            Set<String> cluster = new HashSet<>();
            for (Node n : component) {
                cluster.add(n.getId());
            }
            equalClusters.add(cluster);
        }

        // sanity check: all nodes from the clusters must be unique
        Set<String> uniqueSetOfNodes = new HashSet<>();
        for (Set<String> set : equalClusters) {
            for (String node : set) {
                if (uniqueSetOfNodes.contains(node)) {
                    throw new IllegalStateException(
                            "Wrong mapping of nodes to clusters, duplicate entry: " + node + ",\n"
                                    + equalClusters);
                }

                uniqueSetOfNodes.add(node);
            }
        }

        return equalClusters;
    }

    /**
     * Given a set of argument pairs (= vertices), builds a directed (not necessarily acyclic)
     * graph. It clusters all arguments (= nodes) connected by "equal" relation into a single
     * node with a long name
     *
     * @param argumentPairs argument pairs
     * @return graph
     */
    public static Graph buildGraphFromArgumentPairs(List<AnnotatedArgumentPair> argumentPairs)
            throws InterruptedException
    {
        Graph graph = new SingleGraph("g");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        // so what are now the nodes (clusters?)
        Set<Set<String>> equalClusters = buildEquivalencyClusters(argumentPairs);

        // first add nodes that represent the clusters
        // update source and target id
        for (Set<String> cluster : equalClusters) {
            String nodeID = StringUtils.join(cluster, "+");
            graph.addNode(nodeID);
        }

        //        System.out.println("Equal clusters: " + equalClusters);

        // and now build the graph
        for (AnnotatedArgumentPair annotatedArgumentPair : argumentPairs) {
            String goldLabel = annotatedArgumentPair.getGoldLabel();

            //            System.out.println("Adding edge: " + annotatedArgumentPair.toStringSimple());

            // we're skipping equal pairs, these are already clustered in a single node
            // so there's no need to add edges
            if (!LABEL_EQUAL.equals(goldLabel)) {
                double priorEdgeWeight = computeEdgeWeight(annotatedArgumentPair, LAMBDA_PENALTY);

                // gold is a1 (default)
                String sourceId = annotatedArgumentPair.getArg1().getId();
                String targetId = annotatedArgumentPair.getArg2().getId();

                // gold is a2
                if ("a2".equals(goldLabel)) {
                    sourceId = annotatedArgumentPair.getArg2().getId();
                    targetId = annotatedArgumentPair.getArg1().getId();
                }

                Set<String> sourceCluster = null;
                Set<String> targetCluster = null;

                // first find the mapping from nodeId to its cluster (for clustering "equal" nodes)
                for (Set<String> cluster : equalClusters) {
                    if (cluster.contains(sourceId)) {
                        sourceCluster = cluster;
                    }

                    if (cluster.contains(targetId)) {
                        targetCluster = cluster;
                    }
                }

                // update source and target id
                if (sourceCluster != null) {
                    sourceId = StringUtils.join(sourceCluster, "+");
                }
                if (targetCluster != null) {
                    targetId = StringUtils.join(targetCluster, "+");
                }

                // would this edge introduce a self-loop? (it's possible)
                // will be revealed later
                boolean selfLoop = false;

                if (sourceId.equals(targetId)) {
                    //                    System.err.println("Argument pair " + annotatedArgumentPair.toStringSimple()
                    //                            + " caused self-loop " + sourceId);
                    selfLoop = true;
                }

                String edgeId = sourceId + ":" + targetId;
                Edge edge = graph.addEdge(edgeId, sourceId, targetId, true);

                if (selfLoop) {
                    //                    System.err.println(edge);
                }

                // if the edge does not exist yet
                if (edge != null) {
                    edge.setAttribute(EDGE_PRIOR_WEIGHT, priorEdgeWeight);

                    edge.getSourceNode().addAttribute("ui.label", sourceId);
                    edge.getTargetNode().addAttribute("ui.label", targetId);

                    // update the properties of nodes as well
                    edge.getSourceNode().setAttribute(NODE_ATTR_CLUSTERED_ARGS, sourceCluster);
                    edge.getTargetNode().setAttribute(NODE_ATTR_CLUSTERED_ARGS, targetCluster);
                }
            }
        }

        //        System.out.println("clusters" + equalClusters.size());
        //        System.out.println("graphNodeCount" + graph.getNodeCount());
        // sanity check

        if (equalClusters.size() > graph.getNodeCount() && graph.getNodeCount() > 0) {
            System.err.println(equalClusters);
            System.err.println(graph.getNodeCount());
            graph.display();
            Thread.sleep(5000);
            throw new IllegalStateException("More nodes than expected",
                    new IOException(argumentPairs.get(0).toString()));
        }

        return graph;
    }

    /**
     * Merges clusters that contain common elements into one cluster. Internally uses
     * connected components graph algorithm.
     *
     * @param equalClusters clusters
     * @return new set of clusters
     */
    public static Set<Set<String>> mergeClusters(Set<Set<String>> equalClusters)
    {
        // create a new undirected graph
        Graph graph = new DefaultGraph("CC Test");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        // add all "edges"; for each pair from each cluster
        for (Set<String> cluster : equalClusters) {
            List<String> clusterList = new ArrayList<>(cluster);

            for (int i = 0; i < clusterList.size(); i++) {
                for (int j = i + 1; j < clusterList.size(); j++) {
                    // edge name
                    String iName = clusterList.get(i);
                    String jName = clusterList.get(j);

                    List<String> names = Arrays.asList(iName, jName);
                    Collections.sort(names);
                    String edgeName = StringUtils.join(names, "_");

                    graph.addEdge(edgeName, iName, jName);
                }
            }
        }

        // compute connected components
        ConnectedComponents cc = new ConnectedComponents();
        cc.init(graph);

        Set<Set<String>> result = new HashSet<>();

        cc.setCountAttribute("cluster");
        cc.compute();

        //        System.out.println(cc.getConnectedComponentsCount());

        // re-create clusters from all connected components
        for (ConnectedComponents.ConnectedComponent component : cc) {
            Set<String> cluster = new HashSet<>();
            for (Node n : component) {
                cluster.add(n.getId());
            }
            result.add(cluster);
        }

        //        System.out.println(result);

        return result;
    }

    /*
    private static boolean hasOverlappingClusters(Set<Set<String>> clusters)
    {
        Set<String> uniqueSetOfNodes = new HashSet<>();
        for (Set<String> set : clusters) {
            for (String node : set) {
                if (uniqueSetOfNodes.contains(node)) {
                    return true;
                }

                uniqueSetOfNodes.add(node);
            }
        }

        return false;
    }
    */

    /**
     * Computes the weight of the argument pair given the workers' scores from MACE. Labels
     * different from the gold predicted label are penalized by the {@code lambda} parameter.
     * Output is squeezed by sigmoid function to fit into (0-1)
     *
     * @param annotatedArgumentPair argument pair
     * @return weight (0.0 - 1.0)
     */
    public static double computeEdgeWeight(AnnotatedArgumentPair annotatedArgumentPair,
            double lambda)
    {
        String goldLabel = annotatedArgumentPair.getGoldLabel();

        if (goldLabel == null) {
            throw new IllegalArgumentException(
                    "Cannot compute weight on argument pair with null gold label");
        }

        double sumCompetenceGold = 0.0;
        double sumCompetenceOpposite = 0.0;

        for (AnnotatedArgumentPair.MTurkAssignment assignment : annotatedArgumentPair
                .getmTurkAssignments()) {
            // label
            String label = assignment.getValue();
            double competence = assignment.getTurkCompetence();

            if (label.equals(goldLabel)) {
                sumCompetenceGold += competence;
            }
            else {
                sumCompetenceOpposite += competence;
            }
        }

        double sum = sumCompetenceGold - (lambda * sumCompetenceOpposite);

        // and squeeze using sigmoid
        return SIGMOID.value(sum);
    }

    @SuppressWarnings("unchecked")
    public static void collectResults(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<File> files = new ArrayList<>(IOHelper.listXmlFiles(new File(inputDir)));

        Map<String, Map<String, GraphCleaningResults>> results = new TreeMap<>();

        List<ArgumentPairListSorter> sortingAlgorithms = Arrays
                .asList(new RandomArgumentPairListSorter(1),
                        new AscendingScoreArgumentPairListSorter(),
                        new DescendingScoreArgumentPairListSorter());

        for (File file : files) {
            Map<String, GraphCleaningResults> resultsForSingleFile = new HashMap<>();
            results.put(file.getName(), resultsForSingleFile);

            List<Boolean> removeEqualPairsParams = Arrays.asList(true, false);

            for (Boolean removeEqualEdgesParam : removeEqualPairsParams) {

                for (ArgumentPairListSorter sorter : sortingAlgorithms) {
                    Step6GraphTransitivityCleaner transitivityCleaner = new Step6GraphTransitivityCleaner(
                            sorter, removeEqualEdgesParam);

                    String prefix = (removeEqualEdgesParam ? "no-eq_" : "all_") + sorter.getClass()
                            .getSimpleName();

                    GraphCleaningResults singleResult = transitivityCleaner
                            .processSingleFile(file, outputDir, prefix, removeEqualEdgesParam);

                    resultsForSingleFile.put(prefix, singleResult);
                }
            }
        }

        System.out.println(results);

        XStreamTools.getXStream()
                .toXML(results, new FileOutputStream(new File(outputDir, "all-results-step6.xml")));
    }

    @SuppressWarnings("unchecked")
    public static void printResultStatistics(File xmlFile)
            throws IllegalAccessException
    {
        Map<String, Map<String, GraphCleaningResults>> results = (Map<String, Map<String, GraphCleaningResults>>) XStreamTools
                .getXStream().fromXML(xmlFile);

        //        System.out.println(results);

        SortedMap<String, List<GraphCleaningResults>> resultsGroupedByMethod = new TreeMap<>();

        for (Map.Entry<String, Map<String, GraphCleaningResults>> entry : results.entrySet()) {
            //            System.out.println(entry.getKey());

            for (Map.Entry<String, GraphCleaningResults> e : entry.getValue().entrySet()) {
                //                System.out.println(e.getKey());
                //                System.out.println(e.getValue());

                if (!resultsGroupedByMethod.containsKey(e.getKey())) {
                    resultsGroupedByMethod.put(e.getKey(), new ArrayList<GraphCleaningResults>());
                }

                resultsGroupedByMethod.get(e.getKey()).add(e.getValue());
            }
        }

        String header = null;

        // collect statistics
        for (Map.Entry<String, List<GraphCleaningResults>> entry : resultsGroupedByMethod
                .entrySet()) {
            List<GraphCleaningResults> value = entry.getValue();
            SortedMap<String, DescriptiveStatistics> stringDescriptiveStatisticsMap = collectStatisticsOverGraphCleaningResults(
                    value);

            if (header == null) {
                header = StringUtils.join(stringDescriptiveStatisticsMap.keySet(), "\t");
                System.out.println("\t\t" + header);
            }

            List<Double> means = new ArrayList<>();
            List<Double> stdDevs = new ArrayList<>();
            for (DescriptiveStatistics statistics : stringDescriptiveStatisticsMap.values()) {
                means.add(statistics.getMean());
                stdDevs.add(statistics.getStandardDeviation());
            }

            List<String> meansString = new ArrayList<>();
            for (Double mean : means) {
                meansString.add(String.format(Locale.ENGLISH, "%.2f", mean));
            }

            List<String> stdDevString = new ArrayList<>();
            for (Double stdDev : stdDevs) {
                stdDevString.add(String.format(Locale.ENGLISH, "%.2f", stdDev));
            }

            System.out.println(entry.getKey() + "\tmean\t" + StringUtils.join(meansString, "\t"));
            //            System.out.println(entry.getKey() + "\tstdDev\t" + StringUtils.join(stdDevString, "\t"));
        }
    }

    public static SortedMap<String, DescriptiveStatistics> collectStatisticsOverGraphCleaningResults(
            Collection<GraphCleaningResults> results)
            throws IllegalAccessException
    {

        SortedMap<String, DescriptiveStatistics> result = new TreeMap<>();

        for (GraphCleaningResults r : results) {
            Field[] declaredFields = GraphCleaningResults.class.getDeclaredFields();
            //            System.out.println(Arrays.toString(declaredFields));
            for (Field field : declaredFields) {
                String fieldName = field.getName();

                if (!result.containsKey(fieldName)) {
                    result.put(fieldName, new DescriptiveStatistics());
                }

                Object value = field.get(r);

                double doubleVal;

                if (value instanceof Integer) {
                    doubleVal = ((Integer) value).doubleValue();
                }
                else if (value instanceof Double) {
                    doubleVal = (Double) value;
                }
                else {
                    throw new IllegalStateException("Unkown type " + value.getClass());
                }

                //                System.out.println(doubleVal);

                result.get(fieldName).addValue(doubleVal);
            }

        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        collectResults(args);
        //        printResultStatistics(new File(new File(args[1]), "all-results-step6.xml"));

    }

}
