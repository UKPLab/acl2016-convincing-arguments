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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.graph;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.algorithm.BellmanFord;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling.Step6GraphTransitivityCleaner;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class VisualizationTest
{
    private static final String WEIGHT = "weight";

    public static void main(String[] args)
            throws IOException
    {
        DefaultGraph g = new DefaultGraph("my beautiful graph");

        //        DGSParser parser = new DGSParser(new FileSourceDGS(), new FileReader(new File("/tmp/graph.dgs")));
        //        Parser p = fileSourceDGS.getNewParserFactory()
        //                .newParser(new FileReader("/tmp/graph.dgs"));

        FileSourceDGS fileSourceDGS = new FileSourceDGS();
        Graph graph = new DefaultGraph("g");
        fileSourceDGS.addSink(graph);
        //        fileSourceDGS.readAll(
        //                "/tmp/if-your-spouse-committed-murder-and-he-or-she-confided-in-you-would-you-turn-them-in-_no.xml.dgs");

        fileSourceDGS.readAll(
                //                "~/data2/convincingness/step6-graph-cleaning/is-it-better-to-have-a-lousy-father-or-to-be-fatherless-_fatherless.xml.dgs");
                "/home/user-ukp/data2/convincingness/step6-clean-graphs/no-eq_AscendingScoreArgumentPairListSorterchristianity-or-atheism-_atheism.xml.dgs");

        File graphFile = new File(
                "/tmp/out.png");

        showGraph(graph, graphFile);
    }

    public static void showGraph(Graph graph, File outputFile)
            throws IOException
    {
        graph.addAttribute("ui.antialias", true);
        graph.addAttribute("ui.stylesheet", "edge {fill-color:grey; shape: cubic-curve;} ");

        // min and max ratio
        int minRatio = Integer.MAX_VALUE;
        int maxRatio = Integer.MIN_VALUE;
        for (Node node : graph) {
            // get ratio
            int ratio = node.getOutDegree() - node.getInDegree() + 100;

            minRatio = Math.min(ratio, minRatio);
            maxRatio = Math.max(ratio, maxRatio);
        }

        for (Node node : graph) {
            // parse cluster node IDs back to sorted set
            SortedSet<String> clusterNodesIDs = new TreeSet<>(Arrays.asList(
                    node.getAttribute(Step6GraphTransitivityCleaner.NODE_ATTR_CLUSTERED_ARGS)
                            .toString().replaceAll("^\\[", "").replaceAll("\\]$", "").split(", ")));

            // get longest out-coming transition path

            // get ratio
            int ratio = node.getOutDegree() - node.getInDegree() + 100;

            String color = mapValueToColor(minRatio, maxRatio, ratio);
            int size = (node.getOutDegree() + 10) * 2;

            int maxTransitivityScore = computeMaxTransitivityScore(graph, node);

            node.addAttribute("ui.style",
                    String.format("fill-color: %s; size: %dpx;", color, size));
            node.addAttribute("ui.label",
                    String.format("%d/%d/%d", node.getInDegree(), node.getOutDegree(),
                            maxTransitivityScore));
        }

        final int RES = 450;
        Viewer viewer = graph.display();
        ViewPanel panel = viewer.getDefaultView();
        panel.setSize(RES, RES);

        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG,
                FileSinkImages.Resolutions.HD720);

        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
        pic.setResolution(RES, RES);

        pic.writeAll(graph, outputFile.getAbsolutePath());
    }

    /**
     * Computes max. transitivity score for the given node (as described in the paper)
     * <p/>
     * It uses Bellman-Ford algorithm to compute the shortest and longest paths
     *
     * @param graph graph (must be DAG)
     * @return statistics
     */
    private static int computeMaxTransitivityScore(Graph graph, Node sourceNode)
    {
        if (sourceNode.getOutDegree() == 0) {
            return 0;
        }

        // find all out-degree > 1 nodes
        Set<Node> targetNodes = new HashSet<>();
        for (Node n : graph) {
            if (n.getInDegree() > 1) {
                targetNodes.add(n);
            }
        }

        FileSourceDGS source = new FileSourceDGS();
        source.addSink(graph);

        DescriptiveStatistics result = new DescriptiveStatistics();

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

        int maxTransitivityScore = Integer.MIN_VALUE;

        for (Node targetNode : targetNodes) {
            Path shortestPath = bfShortest.getShortestPath(targetNode);
            Path longestPath = bfLongest.getShortestPath(targetNode);

            int shortestPathLength = shortestPath.getEdgeCount();
            int longestPathLength = longestPath.getEdgeCount();

            if (shortestPathLength == 1 && longestPathLength > 1) {
                // update statistics
                maxTransitivityScore = Math.max(maxTransitivityScore, longestPathLength);
            }
        }

        // none found
        maxTransitivityScore = Math.max(maxTransitivityScore, 0);

        return maxTransitivityScore;
    }

    public static String mapValueToColor(int min, int max, int value)
    {
        String[] palette = { "#ffffe0", "#ffdeb8", "#ffbc94", "#ff9777", "#ff6a62", "#ed4256",
                "#d22047", "#af062c", "#8b0000" };

        double a = value - min;
        double b = max - min;

        //        System.out.println(a + ", " + b);
        int index = palette.length - 1 - (int) ((a / b) * (palette.length - 1.0));

        //        System.out.println(index);
        //        System.out.println("---------------");

        return palette[index] + "88";
    }
}
