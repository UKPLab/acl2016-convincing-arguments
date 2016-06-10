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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Habernal
 */
public class Step5bGoldLabelStatistics
{

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        int totalPairsWithReasonSameAsGold = 0;

        DescriptiveStatistics ds = new DescriptiveStatistics();

        DescriptiveStatistics statsPerTopic = new DescriptiveStatistics();

        Map<String, Integer> goldDataDistribution = new HashMap<>();

        int totalGoldReasonTokens = 0;

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);

            int pairsPerTopicCounter = 0;

            for (AnnotatedArgumentPair annotatedArgumentPair : argumentPairs) {
                String goldLabel = annotatedArgumentPair.getGoldLabel();

                int sameInOnePair = 0;

                if (goldLabel != null) {
                    if (!goldDataDistribution.containsKey(goldLabel)) {
                        goldDataDistribution.put(goldLabel, 0);
                    }

                    goldDataDistribution.put(goldLabel, goldDataDistribution.get(goldLabel) + 1);

                    // get gold reason statistics
                    for (AnnotatedArgumentPair.MTurkAssignment assignment : annotatedArgumentPair.mTurkAssignments) {
                        String label = assignment.getValue();

                        if (goldLabel.equals(label)) {
                            sameInOnePair++;

                            totalGoldReasonTokens += assignment.getReason().split("\\W+").length;
                        }
                    }

                    pairsPerTopicCounter++;
                }

                ds.addValue(sameInOnePair);
                totalPairsWithReasonSameAsGold += sameInOnePair;

            }

            statsPerTopic.addValue(pairsPerTopicCounter);
        }

        System.out.println("Total reasons matching gold " + totalPairsWithReasonSameAsGold);
        System.out.println(ds);

        int totalPairs = 0;
        for (Integer pairs : goldDataDistribution.values()) {
            totalPairs += pairs;
        }

        System.out.println(goldDataDistribution);
        System.out.println(goldDataDistribution.values());
        System.out.println("Total pairs: " + totalPairs);

        System.out.println("Stats per topic: "+ statsPerTopic);
        System.out.println("Total gold reason tokens: "+ totalGoldReasonTokens);

    }

}
