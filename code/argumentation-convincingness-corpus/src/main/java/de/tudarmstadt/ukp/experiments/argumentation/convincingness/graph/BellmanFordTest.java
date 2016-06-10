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

import org.graphstream.algorithm.BellmanFord;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.IOException;

/**
 * @author Ivan Habernal
 */
public class BellmanFordTest
{
    //     B-(1)-C
    //    /       \
    //  (1)       (10)
    //  /           \
    // A             F
    //  \           /
    //  (1)       (1)
    //    \       /
    //     D-(1)-E
    static String my_graph =
            "DGS004\n"
                    + "my 0 0\n"
                    + "an A \n"
                    + "an B \n"
                    + "an C \n"
                    + "an D \n"
                    + "an E \n"
                    + "an F \n"
                    + "ae AB A B weight:1.0 \n"
                    + "ae AD A D weight:1.0 \n"
                    + "ae BC B C weight:1.0 \n"
                    + "ae CF C F weight:10.0 \n"
                    + "ae DE D E weight:1.0 \n"
                    + "ae EF E F weight:1.0 \n";

    public static void main(String[] args)
            throws IOException
    {
        Graph graph = new DefaultGraph("Bellman-Ford Test");
        //        StringReader reader  = new StringReader(my_graph);

        graph.setAutoCreate(true);
        graph.setStrict(false);
        Edge e1 = graph.addEdge("1", "A", "B", true);
        Edge e2 = graph.addEdge("2", "B", "C", true);
        Edge e3 = graph.addEdge("3", "C", "D", true);
        Edge e4 = graph.addEdge("4", "A", "D", true);
        Edge e5 = graph.addEdge("5", "X", "Y", true);

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("weight", -1.0);
        }

        FileSourceDGS source = new FileSourceDGS();
        source.addSink(graph);
        //        source.readAll(reader);

        BellmanFord bf = new BellmanFord("weight", "A");
        bf.init(graph);
        bf.compute();

        System.out.println(bf.getShortestPath(graph.getNode("X")));
    }
}
