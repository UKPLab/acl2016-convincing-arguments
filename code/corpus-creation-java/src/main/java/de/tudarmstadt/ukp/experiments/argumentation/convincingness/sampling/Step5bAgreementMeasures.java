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

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.util.*;

/**
 * Run this class against estimated gold data from 01-pilot.task.results.tsv
 * to obtain the average rank of the best-ranked worker per argument pair and her performance
 * towards gold data.
 * <p/>
 * Section 'Quality control' in the article.
 *
 * @author Ivan Habernal
 */
public class Step5bAgreementMeasures
{
    private static final int TOP_K_VOTES = 1;

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        // all annotations
        List<AnnotatedArgumentPair> allArgumentPairs = new ArrayList<>();

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        for (File file : files) {
            allArgumentPairs
                    .addAll((List<AnnotatedArgumentPair>) XStreamTools.getXStream().fromXML(file));
        }

        // for collecting the rank of n-th best worker per HIT
        SortedMap<Integer, DescriptiveStatistics> nThWorkerOnHITRank = new TreeMap<>();
        // confusion matrix wrt. gold data for each n-th best worker on HIT
        SortedMap<Integer, ConfusionMatrix> nThWorkerOnHITConfusionMatrix = new TreeMap<>();

        // initialize maps
        for (int i = 0; i < TOP_K_VOTES; i++) {
            nThWorkerOnHITRank.put(i, new DescriptiveStatistics());
            nThWorkerOnHITConfusionMatrix.put(i, new ConfusionMatrix());
        }

        for (AnnotatedArgumentPair argumentPair : allArgumentPairs) {
            // sort turker rank and their vote
            SortedMap<Integer, String> rankAndVote = new TreeMap<>();

            System.out.println(argumentPair.mTurkAssignments.size());

            for (AnnotatedArgumentPair.MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                rankAndVote.put(assignment.getTurkRank(), assignment.getValue());
            }

            String goldLabel = argumentPair.getGoldLabel();

            System.out.println(rankAndVote);

            // top K workers for the HIT
            List<String> topKVotes = new ArrayList<>(rankAndVote.values()).subList(0, TOP_K_VOTES);

            // rank of top K workers
            List<Integer> topKRanks = new ArrayList<>(rankAndVote.keySet()).subList(0, TOP_K_VOTES);

            System.out.println("Top K votes: " + topKVotes);
            System.out.println("Top K ranks: " + topKRanks);

            // extract only category (a1, a2, or equal)
            List<String> topKVotesOnlyCategory = new ArrayList<>();
            for (String vote : topKVotes) {
                String category = vote.split("_")[2];
                topKVotesOnlyCategory.add(category);
            }

            System.out.println(
                    "Top " + TOP_K_VOTES + " workers' decisions: " + topKVotesOnlyCategory);

            if (goldLabel == null) {
                System.out.println("No gold label estimate for " + argumentPair.getId());
            }
            else {
                // update statistics
                for (int i = 0; i < TOP_K_VOTES; i++) {
                    nThWorkerOnHITConfusionMatrix.get(i)
                            .increaseValue(goldLabel, topKVotesOnlyCategory.get(i));

                    // rank is +1 (we don't start ranking from zero)
                    nThWorkerOnHITRank.get(i).addValue(topKRanks.get(i) + 1);
                }
            }
        }

        for (int i = 0; i < TOP_K_VOTES; i++) {
            System.out.println("n-th worker : " + (i + 1) + " -----------");
            System.out.println(nThWorkerOnHITConfusionMatrix.get(i).printNiceResults());
            System.out.println(nThWorkerOnHITConfusionMatrix.get(i));
            System.out.println("Average rank: " + nThWorkerOnHITRank.get(i).getMean() + ", stddev "
                    + nThWorkerOnHITRank.get(i).getStandardDeviation());
        }

    }
}
