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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * This produces the ConvArgStrict
 *
 * @author Ivan Habernal
 */
public class Step7aLearningDataProducer
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws IOException
    {
        String inputDir = args[0];
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        // for generating ConvArgStrict use this
        String prefix = "no-eq_DescendingScoreArgumentPairListSorter";

        // for oversampling using graph transitivity properties use this
//        String prefix = "generated_no-eq_AscendingScoreArgumentPairListSorter";

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();

            if (!file.getName().startsWith(prefix)) {
                iterator.remove();
            }
        }

        int totalGoldPairsCounter = 0;

        Map<String, Integer> goldDataDistribution = new HashMap<>();

        DescriptiveStatistics statsPerTopic = new DescriptiveStatistics();

        int totalPairsWithReasonSameAsGold = 0;

        DescriptiveStatistics ds = new DescriptiveStatistics();

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);

            int pairsPerTopicCounter = 0;

            String name = file.getName().replaceAll(prefix, "").replaceAll("\\.xml", "");

            PrintWriter pw = new PrintWriter(new File(outputDir, name + ".csv"), "utf-8");

            pw.println("#id\tlabel\ta1\ta2");

            for (AnnotatedArgumentPair argumentPair : argumentPairs) {
                String goldLabel = argumentPair.getGoldLabel();

                if (!goldDataDistribution.containsKey(goldLabel)) {
                    goldDataDistribution.put(goldLabel, 0);
                }

                goldDataDistribution.put(goldLabel, goldDataDistribution.get(goldLabel) + 1);

                pw.printf(Locale.ENGLISH, "%s\t%s\t%s\t%s%n",
                        argumentPair.getId(), goldLabel,
                        multipleParagraphsToSingleLine(argumentPair.getArg1().getText()),
                        multipleParagraphsToSingleLine(argumentPair.getArg2().getText())
                );

                pairsPerTopicCounter++;

                int sameInOnePair = 0;

                // get gold reason statistics
                for (AnnotatedArgumentPair.MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                    String label = assignment.getValue();

                    if (goldLabel.equals(label)) {
                        sameInOnePair++;
                    }
                }

                ds.addValue(sameInOnePair);
                totalPairsWithReasonSameAsGold += sameInOnePair;
            }

            totalGoldPairsCounter += pairsPerTopicCounter;
            statsPerTopic.addValue(pairsPerTopicCounter);

            pw.close();
        }

        System.out.println("Total reasons matching gold " + totalPairsWithReasonSameAsGold);
        System.out.println(ds);


        System.out.println("Total gold pairs: " + totalGoldPairsCounter);
        System.out.println(statsPerTopic);

        int totalPairs = 0;
        for (Integer pairs : goldDataDistribution.values()) {
            totalPairs += pairs;
        }
        System.out.println("Total pairs: " + totalPairs);
        System.out.println(goldDataDistribution);

    }

    public static String multipleParagraphsToSingleLine(String s)
    {
        return s.replaceAll("\n", " <br/> ");
    }
}
