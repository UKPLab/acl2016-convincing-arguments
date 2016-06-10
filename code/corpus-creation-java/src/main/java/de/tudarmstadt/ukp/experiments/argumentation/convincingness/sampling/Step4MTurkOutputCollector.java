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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step4MTurkOutputCollector

{

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy",
            Locale.US);

    // skip rejected assignments?
    private static final boolean SKIP_REJECTED = true;

    // we take max 5 assignments
    private static final int MAXIMUM_ASSIGNMENTS_PER_HIT = 5;

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDirWithArgumentPairs = args[0];

        File[] resultFiles;

        if (args[1].contains("*")) {
            File path = new File(args[1]);
            File directory = path.getParentFile();
            String regex = path.getName().replaceAll("\\*", "");

            List<File> files = new ArrayList<>(
                    FileUtils.listFiles(directory, new String[] { regex }, false));
            resultFiles = new File[files.size()];
            for (int i = 0; i < files.size(); i++) {
                resultFiles[i] = files.get(i);
            }
        }
        else {
            // result file is a comma-separated list of CSV files from MTurk
            String[] split = args[1].split(",");
            resultFiles = new File[split.length];
            for (int i = 0; i < split.length; i++) {
                resultFiles[i] = new File(split[i]);
            }
        }

        File outputDir = new File(args[2]);

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Cannot create directory " + outputDir);
            }
        }

        // error if output folder not empty to prevent any confusion by mixing files
        if (!FileUtils.listFiles(outputDir, null, false).isEmpty()) {
            throw new IllegalArgumentException("Output dir " + outputDir + " is not empty");
        }

        // collected assignments with empty reason for rejections
        Set<String> assignmentsWithEmptyReason = new HashSet<>();

        // parse with first line as header
        MTurkOutputReader mTurkOutputReader = new MTurkOutputReader(resultFiles);

        Collection<File> files = FileUtils
                .listFiles(new File(inputDirWithArgumentPairs), new String[] { "xml" }, false);

        if (files.isEmpty()) {
            throw new IOException("No xml files found in " + inputDirWithArgumentPairs);
        }

        // statistics: how many hits with how many assignments ; hit ID / assignments
        Map<String, Map<String, Integer>> assignmentsPerHits = new HashMap<>();

        // collect accept/reject statistics
        for (Map<String, String> record : mTurkOutputReader) {
            boolean wasRejected = "Rejected".equals(record.get("assignmentstatus"));
            String hitID = record.get("hitid");
            String hitTypeId = record.get("hittypeid");

            if (!wasRejected) {
                // update statistics
                if (!assignmentsPerHits.containsKey(hitTypeId)) {
                    assignmentsPerHits.put(hitTypeId, new HashMap<String, Integer>());
                }

                if (!assignmentsPerHits.get(hitTypeId).containsKey(hitID)) {
                    assignmentsPerHits.get(hitTypeId).put(hitID, 0);
                }

                assignmentsPerHits.get(hitTypeId)
                        .put(hitID, assignmentsPerHits.get(hitTypeId).get(hitID) + 1);
            }
        }

        // statistics: how many hits with how many assignments ; hit ID / assignments
        Map<String, Integer> approvedAssignmentsPerHit = new HashMap<>();
        Map<String, Integer> rejectedAssignmentsPerHit = new HashMap<>();

        // collect accept/reject statistics
        for (Map<String, String> record : mTurkOutputReader) {
            boolean approved = "Approved".equals(record.get("assignmentstatus"));
            boolean rejected = "Rejected".equals(record.get("assignmentstatus"));
            String hitID = record.get("hitid");

            if (approved) {
                // update statistics
                if (!approvedAssignmentsPerHit.containsKey(hitID)) {
                    approvedAssignmentsPerHit.put(hitID, 0);
                }

                approvedAssignmentsPerHit.put(hitID, approvedAssignmentsPerHit.get(hitID) + 1);
            }
            else if (rejected) {
                // update statistics
                if (!rejectedAssignmentsPerHit.containsKey(hitID)) {
                    rejectedAssignmentsPerHit.put(hitID, 0);
                }

                rejectedAssignmentsPerHit.put(hitID, rejectedAssignmentsPerHit.get(hitID) + 1);
            }
            else {
                throw new IllegalStateException(
                        "Unknown state: " + record.get("assignmentstatus") + " HITID: " + hitID);
            }
        }

        //        System.out.println("Approved: " + approvedAssignmentsPerHit);
        //        System.out.println("Rejected: " + rejectedAssignmentsPerHit);

        System.out.println("Approved (values): " + new HashSet<>(approvedAssignmentsPerHit.values()));
        System.out.println("Rejected (values): " + new HashSet<>(rejectedAssignmentsPerHit.values()));
        // rejection statistics
        int totalRejected = 0;
        for (Map.Entry<String, Integer> rejectionEntry : rejectedAssignmentsPerHit.entrySet()) {
            totalRejected += rejectionEntry.getValue();
        }

        System.out.println("Total rejections: " + totalRejected);

        /*
        // generate .success files for adding more annotations
        for (File resultFile : resultFiles) {
            String hitTypeID = mTurkOutputReader.getHitTypeIdForFile().get(resultFile);

            // assignments for that hittypeid (= file)
            Map<String, Integer> assignments = assignmentsPerHits.get(hitTypeID);

            prepareUpdateHITsFiles(assignments, hitTypeID, resultFile);
        }
        */

        int totalSavedPairs = 0;

        // load all previously prepared argument pairs
        for (File file : files) {
            List<ArgumentPair> argumentPairs = (List<ArgumentPair>) XStreamTools.getXStream()
                    .fromXML(file);

            List<AnnotatedArgumentPair> annotatedArgumentPairs = new ArrayList<>();

            for (ArgumentPair argumentPair : argumentPairs) {
                AnnotatedArgumentPair annotatedArgumentPair = new AnnotatedArgumentPair(
                        argumentPair);

                // is there such an answer?
                String key = "Answer." + argumentPair.getId();

                // iterate only if there is such column to save time
                if (mTurkOutputReader.getColumnNames().contains(key)) {
                    // now find the results
                    for (Map<String, String> record : mTurkOutputReader) {
                        if (record.containsKey(key)) {
                            // extract the values
                            AnnotatedArgumentPair.MTurkAssignment assignment = new AnnotatedArgumentPair.MTurkAssignment();

                            boolean wasRejected = "Rejected"
                                    .equals(record.get("assignmentstatus"));

                            // only non-rejected (if required)
                            if (!wasRejected) {
                                String hitID = record.get("hitid");
                                String workerID = record.get("workerid");
                                String assignmentId = record.get("assignmentid");
                                try {
                                    assignment.setAssignmentAcceptTime(
                                            DATE_FORMAT
                                                    .parse(record.get("assignmentaccepttime")));
                                    assignment.setAssignmentSubmitTime(
                                            DATE_FORMAT
                                                    .parse(record.get("assignmentsubmittime")));
                                    assignment.setHitComment(record.get("Answer.feedback"));
                                    assignment.setHitID(hitID);
                                    assignment.setTurkID(workerID);
                                    assignment.setAssignmentId(assignmentId);

                                    // and answer specific fields
                                    String valueRaw = record.get(key);

                                    // so far the label has had format aXXX_aYYY_a1, aXXX_aYYY_a2, or aXXX_aYYY_equal
                                    // strip now only true label
                                    String label = valueRaw.split("_")[2];

                                    assignment.setValue(label);
                                    String reason = record.get(key + "_reason");

                                    // missing reason
                                    if (reason == null) {
                                        assignmentsWithEmptyReason.add(assignmentId);
                                    }
                                    else {
                                        assignment.setReason(reason);

                                        // get worker's stance
                                        String stanceRaw = record.get(key + "_stance");
                                        if (stanceRaw != null) {
                                            // parse stance
                                            String stance = stanceRaw.split("_stance_")[1];
                                            assignment.setWorkerStance(stance);
                                        }

                                        // we take maximal 5 assignments
                                        Collections.sort(annotatedArgumentPair.mTurkAssignments,
                                                new Comparator<AnnotatedArgumentPair.MTurkAssignment>()
                                                {
                                                    @Override public int compare(
                                                            AnnotatedArgumentPair.MTurkAssignment o1,
                                                            AnnotatedArgumentPair.MTurkAssignment o2)
                                                    {
                                                        return o1.getAssignmentAcceptTime()
                                                                .compareTo(
                                                                        o2.getAssignmentAcceptTime());
                                                    }
                                                });

                                        if (annotatedArgumentPair.mTurkAssignments.size()
                                                < MAXIMUM_ASSIGNMENTS_PER_HIT) {
                                            annotatedArgumentPair.mTurkAssignments.add(assignment);
                                        }
                                    }
                                }
                                catch (IllegalArgumentException | NullPointerException ex) {
                                    System.err.println(
                                            "Malformed annotations for HIT " + hitID
                                                    + ", worker "
                                                    + workerID + ", assignment " + assignmentId
                                                    + "; " + ex.getMessage() + ", full record: "
                                                    + record);
                                }
                            }
                        }
                    }
                }

                // and if there are some annotations, add it to the result set
                if (!annotatedArgumentPair.mTurkAssignments.isEmpty()) {
                    annotatedArgumentPairs.add(annotatedArgumentPair);
                }
            }

            if (!annotatedArgumentPairs.isEmpty()) {
                File outputFile = new File(outputDir, file.getName());
                XStreamTools.toXML(annotatedArgumentPairs, outputFile);

                System.out.println(
                        "Saved " + annotatedArgumentPairs.size() + " annotated pairs to "
                                + outputFile);
                totalSavedPairs += annotatedArgumentPairs.size();
            }
        }

        System.out.println("Total saved " + totalSavedPairs + " pairs");

        // print assignments with empty reasons
        if (!assignmentsWithEmptyReason.isEmpty()) {
            System.out.println(
                    "== Assignments with empty reason:\nassignmentIdToReject\tassignmentIdToRejectComment");
            for (String assignmentId : assignmentsWithEmptyReason) {
                System.out.println(assignmentId
                        + "\t\"Dear worker, you did not fill the required field with a reason.\"");
            }
        }

    }

    /**
     * Creates .success files for updating HITs in order to require more assignments.
     *
     * @param assignmentsPerHits actual assignments per HIT
     * @param hitTypeID          type
     * @param resultFile         source MTurk file
     * @throws IOException IO exception
     */

    static void prepareUpdateHITsFiles(Map<String, Integer> assignmentsPerHits, String hitTypeID,
            File resultFile)
            throws IOException
    {
        TreeSet<Integer> assignmentNumbers = new TreeSet<>(assignmentsPerHits.values());

        System.out.println(assignmentsPerHits);

        // how many is required to be fully annotated
        final int fullyAnnotated = 5;

        assignmentNumbers.remove(fullyAnnotated);

        for (Integer i : assignmentNumbers) {
            // output file
            int annotationsRequired = fullyAnnotated - i;
            File file = new File(
                    resultFile + "_requires_extra_assignments_" + annotationsRequired + ".success");
            PrintWriter pw = new PrintWriter(file, "utf-8");
            pw.println("hitid\thittypeid");

            for (Map.Entry<String, Integer> entry : assignmentsPerHits.entrySet()) {
                if (i.equals(entry.getValue())) {
                    pw.println(entry.getKey() + "\t" + hitTypeID);
                }
            }

            pw.close();

            System.out.println(
                    "Extra annotations required (" + annotationsRequired + "), saved to " + file
                            .getAbsolutePath());
        }
    }


}
