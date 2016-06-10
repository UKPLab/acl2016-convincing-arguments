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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.algorithm.PageRank;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Argument;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step7bConvArgRankProducer
{

    @SuppressWarnings("unchecked")
    public static void prepareData(String[] args)
            throws Exception
    {
        String inputDir = args[0];
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<File> files = IOHelper.listXmlFiles(new File(inputDir));

        // take only the gold data for this task
        String prefix = "all_DescendingScoreArgumentPairListSorter";
        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();

            if (!file.getName().startsWith(prefix)) {
                iterator.remove();
            }
        }

        int totalArgumentsCounter = 0;

        DescriptiveStatistics statsPerTopic = new DescriptiveStatistics();

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);

            String name = file.getName().replaceAll(prefix, "").replaceAll("\\.xml", "");

            PrintWriter pw = new PrintWriter(new File(outputDir, name + ".csv"), "utf-8");
            pw.println("#id\trank\targument");

            Graph graph = buildGraphFromPairs(argumentPairs);

            Map<String, Argument> arguments = collectArguments(argumentPairs);

            int argumentsPerTopicCounter = arguments.size();

            PageRank pageRank = new PageRank();
            pageRank.setVerbose(true);
            pageRank.init(graph);

            for (Node node : graph) {
                String id = node.getId();
                double rank = pageRank.getRank(node);

                System.out.println(id);

                Argument argument = arguments.get(id);

                String text = Step7aLearningDataProducer
                        .multipleParagraphsToSingleLine(argument.getText());

                pw.printf(Locale.ENGLISH, "%s\t%.5f\t%s%n", argument.getId(), rank, text);
            }

            totalArgumentsCounter += argumentsPerTopicCounter;
            statsPerTopic.addValue(argumentsPerTopicCounter);

            pw.close();
        }

        System.out.println("Total gold arguments: " + totalArgumentsCounter);
        System.out.println(statsPerTopic);
    }

    private static Map<String, Argument> collectArguments(List<AnnotatedArgumentPair> pairs)
    {
        Map<String, Argument> result = new HashMap<>();

        for (ArgumentPair pair : pairs) {
            result.put(pair.getArg1().getId(), pair.getArg1());
            result.put(pair.getArg2().getId(), pair.getArg2());
        }

        return result;
    }

    /**
     * Build mixed graph (directed and undirected) from the given pairs of arguments and computes
     * PageRank on it
     *
     * @param pairs pairs
     * @return graph
     */
    public static Graph buildGraphFromPairs(List<AnnotatedArgumentPair> pairs)
    {
        Graph graph = new SingleGraph("test");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        for (AnnotatedArgumentPair pair : pairs) {
            String goldLabel = pair.getGoldLabel();

            if ("equal".equals(goldLabel)) {
                graph.addEdge(pair.getId(), pair.getArg1().getId(), pair.getArg2().getId(), false);
            }
            else {

                // default A1 > A2
                String source = pair.getArg1().getId();
                String target = pair.getArg2().getId();

                if ("a2".equals(goldLabel)) {
                    source = pair.getArg2().getId();
                    target = pair.getArg1().getId();
                }

                graph.addEdge(pair.getId(), source, target, true);
            }
        }

        //        graph.display();

        return graph;
    }

    public static String multipleParagraphsToSingleLine(String s)
    {
        return s.replaceAll("\n", " <br/> ");
    }

    public static void main(String[] args)
            throws Exception
    {
        prepareData(args);
    }

}
