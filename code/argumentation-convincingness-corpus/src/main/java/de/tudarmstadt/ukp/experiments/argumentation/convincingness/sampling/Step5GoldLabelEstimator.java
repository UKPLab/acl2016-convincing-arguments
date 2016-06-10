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

import de.tudarmstadt.ukp.experiments.argumentation.convincingness.mace.MACE;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This produces ConvArgAll
 * @author Ivan Habernal
 */
public class Step5GoldLabelEstimator
{

    /**
     * Threshold: only X% best gold estimates will be produced
     */
    public static double MACE_THRESHOLD = 0.95;
    //    public static double MACE_THRESHOLD = 1.0;

    // we cannot ignore equal edges here = they were part of annotations
    //    private static boolean IGNORE_EQUAL = false;

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // we will process only a subset first
        List<AnnotatedArgumentPair> allArgumentPairs = new ArrayList<>();

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        for (File file : files) {
            allArgumentPairs
                    .addAll((List<AnnotatedArgumentPair>) XStreamTools.getXStream().fromXML(file));
        }

        // collect turkers and csv
        List<String> turkerIDs = extractAndSortTurkerIDs(allArgumentPairs);
        String preparedCSV = prepareCSV(allArgumentPairs, turkerIDs);

        // save CSV and run MACE
        Path tmpDir = Files.createTempDirectory("mace");
        File maceInputFile = new File(tmpDir.toFile(), "input.csv");
        FileUtils.writeStringToFile(maceInputFile, preparedCSV, "utf-8");

        File outputPredictions = new File(tmpDir.toFile(), "predictions.txt");
        File outputCompetence = new File(tmpDir.toFile(), "competence.txt");

        // run MACE
        MACE.main(
                new String[] { "--iterations", "500", "--threshold", String.valueOf(MACE_THRESHOLD),
                        "--restarts", "50", "--outputPredictions",
                        outputPredictions.getAbsolutePath(), "--outputCompetence",
                        outputCompetence.getAbsolutePath(), maceInputFile.getAbsolutePath() });

        // read back the predictions and competence
        List<String> predictions = FileUtils.readLines(outputPredictions, "utf-8");

        // check the output
        if (predictions.size() != allArgumentPairs.size()) {
            throw new IllegalStateException(
                    "Wrong size of the predicted file; expected " + allArgumentPairs.size()
                            + " lines but was " + predictions.size());
        }

        String competenceRaw = FileUtils.readFileToString(outputCompetence, "utf-8");
        String[] competence = competenceRaw.split("\t");
        if (competence.length != turkerIDs.size()) {
            throw new IllegalStateException(
                    "Expected " + turkerIDs.size() + " competence number, got "
                            + competence.length);
        }

        // rank turkers by competence
        Map<String, Double> turkerIDCompetenceMap = new TreeMap<>();
        for (int i = 0; i < turkerIDs.size(); i++) {
            turkerIDCompetenceMap.put(turkerIDs.get(i), Double.valueOf(competence[i]));
        }

        // sort by value descending
        Map<String, Double> sortedCompetences = IOHelper.sortByValue(turkerIDCompetenceMap, false);
        System.out.println("Sorted turker competences: " + sortedCompetences);

        // assign the gold label and competence

        for (int i = 0; i < allArgumentPairs.size(); i++) {
            AnnotatedArgumentPair annotatedArgumentPair = allArgumentPairs.get(i);
            String goldLabel = predictions.get(i).trim();

            // might be empty
            if (!goldLabel.isEmpty()) {
                // so far the gold label has format aXXX_aYYY_a1, aXXX_aYYY_a2, or aXXX_aYYY_equal
                // strip now only the gold label
                annotatedArgumentPair.setGoldLabel(goldLabel);
            }

            // update turker competence
            for (AnnotatedArgumentPair.MTurkAssignment assignment : annotatedArgumentPair.mTurkAssignments) {
                String turkID = assignment.getTurkID();

                int turkRank = getTurkerRank(turkID, sortedCompetences);
                assignment.setTurkRank(turkRank);

                double turkCompetence = turkerIDCompetenceMap.get(turkID);
                assignment.setTurkCompetence(turkCompetence);
            }
        }

        // now sort the data back according to their original file name
        Map<String, List<AnnotatedArgumentPair>> fileNameAnnotatedPairsMap = new HashMap<>();
        for (AnnotatedArgumentPair argumentPair : allArgumentPairs) {
            String fileName = IOHelper.createFileName(argumentPair.getDebateMetaData(),
                    argumentPair.getArg1().getStance());

            if (!fileNameAnnotatedPairsMap.containsKey(fileName)) {
                fileNameAnnotatedPairsMap.put(fileName, new ArrayList<AnnotatedArgumentPair>());
            }

            fileNameAnnotatedPairsMap.get(fileName).add(argumentPair);
        }

        // and save them to the output file
        for (Map.Entry<String, List<AnnotatedArgumentPair>> entry : fileNameAnnotatedPairsMap
                .entrySet()) {
            String fileName = entry.getKey();
            List<AnnotatedArgumentPair> argumentPairs = entry.getValue();

            File outputFile = new File(outputDir, fileName);

            // and save all sampled pairs into a XML file
            XStreamTools.toXML(argumentPairs, outputFile);

            System.out.println("Saved " + argumentPairs.size() + " pairs to " + outputFile);
        }

    }

    /**
     * Gets turker's relative rank (1 = the best one, ...) given his/her ID.
     *
     * @param turkerID            id
     * @param sortedCompetenceMap map (turkerID, competence) sorted by values descending!!
     * @return rank
     */
    public static int getTurkerRank(String turkerID, Map<String, Double> sortedCompetenceMap)
    {
        int i = 0;
        for (String s : sortedCompetenceMap.keySet()) {
            if (turkerID.equals(s)) {
                return i;
            }

            i++;
        }

        throw new IllegalStateException(
                "Turker " + turkerID + " not present in " + sortedCompetenceMap);
    }

    /**
     * Extract all unique turker IDs from annotations, and returns them as a sorted list
     *
     * @param argumentPairs argument pairs
     * @return sorted IDs
     */
    public static List<String> extractAndSortTurkerIDs(List<AnnotatedArgumentPair> argumentPairs)
    {
        // first, get the sorted set of all turkers
        SortedSet<String> uniqueTurkerSet = new TreeSet<>();
        for (AnnotatedArgumentPair argumentPair : argumentPairs) {
            for (AnnotatedArgumentPair.MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                uniqueTurkerSet.add(assignment.getTurkID());
            }
        }

        // transform to a list
        return new ArrayList<>(uniqueTurkerSet);
    }

    /**
     * Prepares CSV file for MACE (see http://www.isi.edu/publications/licensed-sw/mace/)
     *
     * @param argumentPairs annotated data
     * @param turkerIDs     sorted list of turker IDs
     * @return single string in the proper MACE format
     */
    public static String prepareCSV(List<AnnotatedArgumentPair> argumentPairs,
            List<String> turkerIDs)
    {
        System.out.println(turkerIDs);

        // for each item we need an array of annotations, i.e.
        // label1,,,label1,label2,label1,,,
        // whose size is number of annotators and annotators are identified by position (array index)

        // storing the resulting lines
        List<String> result = new ArrayList<>();

        for (AnnotatedArgumentPair argumentPair : argumentPairs) {
            // for storing individual assignments
            String[] assignmentsArray = new String[turkerIDs.size()];
            // fill with empty strings
            Arrays.fill(assignmentsArray, "");

            for (AnnotatedArgumentPair.MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                // do we want to include this annotation at all?

                // get the turker index
                int turkerIndex = Collections.binarySearch(turkerIDs, assignment.getTurkID());

                // and set the label on the correct position in the array
                assignmentsArray[turkerIndex] = assignment.getValue();

            }

            // concatenate with comma
            String line = StringUtils.join(assignmentsArray, ",");

            System.out.println(line);

            result.add(line);
        }

        // add empty line at the end
        result.add("");

        return StringUtils.join(result, "\n");
    }
}
