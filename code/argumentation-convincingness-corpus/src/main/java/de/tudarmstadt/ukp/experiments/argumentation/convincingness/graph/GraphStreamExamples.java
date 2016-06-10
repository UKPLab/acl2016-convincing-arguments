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

import org.graphstream.algorithm.PageRank;
import org.graphstream.algorithm.TarjanStronglyConnectedComponents;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;

/**
 * @author Ivan Habernal
 */
public class GraphStreamExamples
{
    public static void main1(String[] args)
            throws IOException
    {

        // Tarjan

        Graph g = new MultiGraph("g");

        g.setAutoCreate(true);
        g.setStrict(false);

        Edge e;

        g.addEdge("AB", "A", "B", true);
        g.addEdge("BC", "B", "C", true);
        g.addEdge("CD", "C", "D", true);
        g.addEdge("DB", "D", "B", true);

        //        Node node = g.getNode(1);
        //        Iterator<Node> depthFirstIterator = node.getDepthFirstIterator();
        //        while (depthFirstIterator.hasNext()) {
        //            Node next = depthFirstIterator.next();
        //            System.out.println(next);
        //        }

        g.display();

        TarjanStronglyConnectedComponents tscc = new TarjanStronglyConnectedComponents();
        tscc.init(g);
        tscc.compute();

        for (Node node : g.getEachNode()) {
            node.addAttribute("label", node.getAttribute(tscc.getSCCIndexAttribute()));
        }

    }

    public static void main(String[] args)
            throws InterruptedException
    {
        Graph graph = new SingleGraph("test");
        graph.addAttribute("ui.antialias", true);
        graph.addAttribute("ui.stylesheet",
                "node {fill-color: red; size-mode: dyn-size;} edge {fill-color:grey;}");
        graph.display();

        DorogovtsevMendesGenerator generator = new DorogovtsevMendesGenerator();
        generator.setDirectedEdges(true, true);
        generator.addSink(graph);

        PageRank pageRank = new PageRank();
        pageRank.setVerbose(true);
        pageRank.init(graph);

        generator.begin();
        while (graph.getNodeCount() < 100) {
            generator.nextEvents();
            for (Node node : graph) {
                double rank = pageRank.getRank(node);
                node.addAttribute("ui.size", 5 + Math.sqrt(graph.getNodeCount() * rank * 20));
                node.addAttribute("ui.label", String.format("%.2f%%", rank * 100));
            }
//            Thread.sleep(1000);
        }

        double totalRank = 0.0;
        for (Node node : graph) {
            double rank = pageRank.getRank(node);
            totalRank += rank;
            System.out.println("Node: " + node.getId() + ", rank: " + rank);
        }

        System.out.println(totalRank);
    }

    public static void pagerank()
    {
         /*
        PageRank pageRank = new PageRank();
        pageRank.setVerbose(true);
        pageRank.init(graph);

        for (Node node : graph) {
            double rank = pageRank.getRank(node);
            node.addAttribute("ui.size", 5 + Math.sqrt(graph.getNodeCount() * rank * 100));
            node.addAttribute("ui.label", String.format("%.2f%%", rank * 100));
        }
        */

        /*
        TarjanStronglyConnectedComponents tscc = new TarjanStronglyConnectedComponents();
        tscc.init(graph);
        tscc.compute();

        for (Node n : graph.getEachNode()) {
            String s = n.getEnteringEdgeSet().size() + ":" + n.getOutDegree();
            System.out.println(s);
            Object attribute = n.getAttribute(tscc.getSCCIndexAttribute());
            n.addAttribute("label", attribute);
            System.out.println(attribute);

        }

        graph.display();
        */



            /*
        for (Map.Entry<String, TreeSet<String>> entry : generatedNodes.entrySet()) {

            // print the output
            PrintWriter pw = new PrintWriter(new File(outputDir, entry.getKey() + ".graphviz.txt"),
                    "utf-8");
            pw.println("digraph debate { overlap=false;\n" + "splines=true;");
            for (String node : entry.getValue()) {
                pw.println(node + ";");
            }
            for (String relation : generatedRelations.get(entry.getKey())) {
                pw.println(relation + ";");
            }
            pw.println("}");

            pw.close();
            //        course -- "C-I" [label="n",len=1.00];
        }
            */

    }

}
