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

import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Debate;
import org.apache.commons.io.FileUtils;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Argument;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.DebateSerializer;

import java.io.File;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step2ArgumentPairsSampling
{

    // how many arguments pro side?
    public static final int MAX_SELECTED_ARGUMENTS_PRO_SIDE =
            Step1DebateFilter.MINIMUM_NUMBER_OF_FIRST_LEVEL_ARGUMENTS_PER_SIDE + 10;

    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        // /tmp
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // pseudo-random
        final Random random = new Random(1);

        int totalPairsCount = 0;

        // read all debates
        for (File file : IOHelper.listXmlFiles(new File(inputDir))) {
            Debate debate = DebateSerializer
                    .deserializeFromXML(FileUtils.readFileToString(file, "utf-8"));

            // get two stances
            SortedSet<String> originalStances = debate.getStances();

            // cleaning: some debate has three or more stances (data are inconsistent)
            // remove those with only one argument
            SortedSet<String> stances = new TreeSet<>();
            for (String stance : originalStances) {
                if (debate.getArgumentsForStance(stance).size() > 1) {
                    stances.add(stance);
                }
            }

            if (stances.size() != 2) {
                throw new IllegalStateException(
                        "2 stances per debate expected, was " + stances.size() + ", " + stances);
            }

            // for each stance, get pseudo-random N arguments
            for (String stance : stances) {
                List<Argument> argumentsForStance = debate.getArgumentsForStance(stance);

                // shuffle
                Collections.shuffle(argumentsForStance, random);

                // and get max first N arguments
                List<Argument> selectedArguments = argumentsForStance
                        .subList(0,
                                argumentsForStance.size() < MAX_SELECTED_ARGUMENTS_PRO_SIDE ?
                                        argumentsForStance.size() :
                                        MAX_SELECTED_ARGUMENTS_PRO_SIDE);

                List<ArgumentPair> argumentPairs = new ArrayList<>();

                // now create pairs
                for (int i = 0; i < selectedArguments.size(); i++) {
                    for (int j = (i + 1); j < selectedArguments.size(); j++) {
                        Argument arg1 = selectedArguments.get(i);
                        Argument arg2 = selectedArguments.get(j);

                        ArgumentPair argumentPair = new ArgumentPair();
                        argumentPair.setDebateMetaData(debate.getDebateMetaData());

                        // assign arg1 and arg2 pseudo-randomly
                        // (not to have the same argument as arg1 all the time)
                        if (random.nextBoolean()) {
                            argumentPair.setArg1(arg1);
                            argumentPair.setArg2(arg2);
                        }
                        else {
                            argumentPair.setArg1(arg2);
                            argumentPair.setArg2(arg1);
                        }

                        // set unique id
                        argumentPair.setId(argumentPair.getArg1().getId() + "_" +
                                argumentPair.getArg2().getId());

                        argumentPairs.add(argumentPair);
                    }
                }

                String fileName = IOHelper.createFileName(debate.getDebateMetaData(), stance);

                File outputFile = new File(outputDir, fileName);

                // and save all sampled pairs into a XML file
                XStreamTools.toXML(argumentPairs, outputFile);

                System.out.println("Saved " + argumentPairs.size() + " pairs to " + outputFile);

                totalPairsCount += argumentPairs.size();
            }

        }

        System.out.println("Total pairs generated: " + totalPairsCount);
    }
}
