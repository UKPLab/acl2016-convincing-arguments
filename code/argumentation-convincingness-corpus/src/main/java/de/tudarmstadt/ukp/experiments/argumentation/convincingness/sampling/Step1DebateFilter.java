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

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Argument;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Debate;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.DebateSerializer;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step1DebateFilter
{
    // median = 36 for first-level arguments
    private static final int MEDIAN = 60;

    // length: <median - range; median + range>
    private static final int ARGUMENT_LENGTH_PLUS_MINUS_RANGE = 50;

    // parameters
    public static final int MINIMUM_NUMBER_OF_FIRST_LEVEL_ARGUMENTS_PER_SIDE = 25;

    public static final int MINIMUM_NUMBER_OF_FIRST_LEVEL_ARGUMENTS_PER_DEBATE = 0;


    // these were manually selected, as they comply with the criteria and are meaningful
    static Set<String> selectedDebates = new HashSet<>(
            Arrays.asList(
                    "http://www.createdebate.com/debate/show/Christianity_or_Atheism",
                    "http://www.createdebate.com/debate/show/If_your_spouse_committed_murder_and_he_or_she_confided_in_you_would_you_turn_them_in",
                    "http://www.createdebate.com/debate/show/Should_physical_education_be_mandatory_in_schools",
                    "http://www.createdebate.com/debate/show/TV_is_better_than_Books",
                    "http://www.createdebate.com/debate/show/Ban_Plastic_Water_Bottles",
                    "http://www.convinceme.net/debates/394/Is-porn-wrong.html",
                    "http://www.convinceme.net/debates/61/Gay-Marriage-Right-or-Wrong.html",
                    "http://www.convinceme.net/debates/233/Pro-Choice-vs-Pro-Life.html",
                    "http://www.createdebate.com/debate/show/William_Farquhar_ought_to_be_honoured_as_the_rightful_founder_of_Singapore",
                    "http://www.createdebate.com/debate/show/Is_the_school_uniform_a_good_or_bad_idea",
                    "http://www.createdebate.com/debate/show/INDIA_HAS_THE_POTENTIAL_TO_LEAD_THE_WORLD",
                    "http://www.createdebate.com/debate/show/Human_Growth_and_Development_Should_parents_use_spanking_as_an_option_to_discipline",
                    "http://www.createdebate.com/debate/show/Which_type_of_endeavor_is_better_a_personal_pursuit_or_advancing_the_common_good",
                    "http://www.convinceme.net/debates/58/Firefox-vs-Internet-Explorer.html",
                    "http://www.createdebate.com/debate/show/Is_it_better_to_have_a_lousy_father_or_to_be_fatherless",
                    "http://www.convinceme.net/debates/90/Evolution-vs-Creation.html"
            ));

    /**
     * Computes statistics over full debate dataset
     *
     * @param inputDir input dir
     * @throws IOException IO exception
     */
    public static void computeRawLengthStatistics(String inputDir)
            throws IOException
    {
        DescriptiveStatistics fullWordCountStatistics = new DescriptiveStatistics();

        // read all debates and filter them
        for (File file : FileUtils.listFiles(new File(inputDir), new String[] { "xml" }, false)) {
            Debate debate = DebateSerializer
                    .deserializeFromXML(FileUtils.readFileToString(file, "utf-8"));

            for (Argument argument : debate.getArgumentList()) {

                // we have a first-level argument
                if (argument.getParentId() == null) {
                    // now check the length
                    int wordCount = argument.getText().split("\\s+").length;

                    fullWordCountStatistics.addValue(wordCount);
                }
            }
        }

        System.out.println("Full word count statistics");
        System.out.println(fullWordCountStatistics);
    }

    /**
     * Processes the debates and extract the required debates with arguments
     *
     * @param inputDir  all debates
     * @param outputDir output
     * @throws IOException IO Exception
     */
    public static void processData(String inputDir, File outputDir)
            throws IOException
    {
        // collect some lengths statistics
        DescriptiveStatistics filteredWordCountStatistics = new DescriptiveStatistics();

        Frequency frequency = new Frequency();

        final int lowerBoundaries = MEDIAN - ARGUMENT_LENGTH_PLUS_MINUS_RANGE;
        final int upperBoundaries = MEDIAN + ARGUMENT_LENGTH_PLUS_MINUS_RANGE;

        // read all debates and filter them
        for (File file : FileUtils.listFiles(new File(inputDir), new String[] { "xml" }, false)) {
            Debate debate = DebateSerializer
                    .deserializeFromXML(FileUtils.readFileToString(file, "utf-8"));

            // only selected debates
            if (selectedDebates.contains(debate.getDebateMetaData().getUrl())) {

                Debate debateCopy = new Debate();
                debateCopy.setDebateMetaData(debate.getDebateMetaData());

                // for counting first level arguments (those without parents) for each of the two stances
                Map<String, Integer> argumentStancesCounts = new TreeMap<>();

                for (Argument argument : debate.getArgumentList()) {
                    boolean keepArgument = false;

                    // hack: clean the data -- update stance for "tv" vs. "TV"
                    if ("tv".equalsIgnoreCase(argument.getStance())) {
                        argument.setStance("TV");
                    }

                    // we have a first-level argument
                    if (argument.getParentId() == null) {
                        // now check the length
                        int wordCount = argument.getText().split("\\s+").length;

                        if (wordCount >= lowerBoundaries && wordCount <= upperBoundaries) {
                            String stance = argument.getStance();

                            // update counts
                            if (!argumentStancesCounts.containsKey(stance)) {
                                argumentStancesCounts.put(stance, 0);
                            }
                            argumentStancesCounts
                                    .put(stance, argumentStancesCounts.get(stance) + 1);

                            // keep it
                            keepArgument = true;

                            // update statistics; delete later
                            filteredWordCountStatistics.addValue(wordCount);
                            frequency.addValue((wordCount / 10) * 10);

                        }
                    }

                    // copy to the result
                    if (keepArgument) {
                        debateCopy.getArgumentList().add(argument);
                    }
                }
                // get number of first-level arguments for each side
                Iterator<Map.Entry<String, Integer>> tempIter = argumentStancesCounts.entrySet()
                        .iterator();

                if (argumentStancesCounts.size() > 2) {
                    //                    System.out.println("More stances: " + argumentStancesCounts);
                }

                Integer val1 = tempIter.hasNext() ? tempIter.next().getValue() : 0;
                Integer val2 = tempIter.hasNext() ? tempIter.next().getValue() : 0;

                if ((val1 + val2) >= MINIMUM_NUMBER_OF_FIRST_LEVEL_ARGUMENTS_PER_DEBATE) {
                    if (val1 >= MINIMUM_NUMBER_OF_FIRST_LEVEL_ARGUMENTS_PER_SIDE
                            && val2 >= MINIMUM_NUMBER_OF_FIRST_LEVEL_ARGUMENTS_PER_SIDE) {
                        System.out.println(debate.getDebateMetaData().getUrl() + "\t" + debate
                                .getDebateMetaData().getTitle() + "\t"
                                + argumentStancesCounts);

                        // write the output
                        String xml = DebateSerializer.serializeToXML(debateCopy);
                        FileUtils.writeStringToFile(new File(outputDir, file.getName()), xml,
                                "utf-8");
                    }
                }
            }
        }
    }

    public static void main(String[] args)
            throws IOException
    {
        String inputDir = args[0];

        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        computeRawLengthStatistics(inputDir);
    }
}
