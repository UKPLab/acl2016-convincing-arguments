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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Run this class against estimated gold data from 01-pilot.task.results.tsv
 * to obtain the average rank of the best-ranked worker per argument pair and her performance
 * towards gold data.
 * <p/>
 * Section 'Quality control' in the article.
 *
 * @author Ivan Habernal
 */
public class Step5cRawCorpusStatistics
{
    private static final int TOP_K_VOTES = 1;

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        Map<String, Map<String, Integer>> stancePerTopics = new HashMap<>();

        int totalAssignments = 0;

        int totalPairs = 0;

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);
            stancePerTopics.put(file.getName(), new HashMap<String, Integer>());
            Map<String, Integer> currentStances = stancePerTopics.get(file.getName());

            for (AnnotatedArgumentPair argumentPair : argumentPairs) {
                totalPairs++;

                if (5 != argumentPair.mTurkAssignments.size()) {
                    System.err.println(argumentPair.mTurkAssignments.size() + ": " + argumentPair.mTurkAssignments.iterator().next().getHitID());
                }

                for (AnnotatedArgumentPair.MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                    String stance = assignment.getWorkerStance();

                    totalAssignments++;

                    // none = not provided
                    if (stance == null) {
                        stance = "none";
                    }

                    if (!currentStances.containsKey(stance)) {
                        currentStances.put(stance, 0);
                    }

                    currentStances.put(stance, currentStances.get(stance) + 1);
                }
            }
        }

        System.out.println(stancePerTopics);

        // total stance
        Map<String, Integer> counts = new HashMap<>();
        counts.put("none", 0);
        counts.put("same", 0);
        counts.put("opposite", 0);

        for (Map<String, Integer> topicStances : stancePerTopics.values()) {
            for (Map.Entry<String, Integer> entry : topicStances.entrySet()) {
                counts.put(entry.getKey(), counts.get(entry.getKey()) + entry.getValue());
            }
        }

        System.out.println(counts);
        System.out.println(totalAssignments);
        System.out.println(totalPairs);
    }

}
