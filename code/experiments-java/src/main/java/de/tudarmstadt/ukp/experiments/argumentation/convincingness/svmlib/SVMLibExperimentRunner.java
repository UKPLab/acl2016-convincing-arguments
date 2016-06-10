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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan Habernal
 */
public class SVMLibExperimentRunner
{
    public static final String SVM_LIB_PATH = "/usr/local/bin/";

    public void runCrossValidation(File dataDir)
            throws IOException
    {
        List<File> files = new ArrayList<File>(FileUtils
                .listFiles(dataDir, new String[] { "libsvm.txt" }, false));

        for (File testFile : files) {
            // create training files from the rest
            Set<File> trainingFiles = new HashSet<File>(files);
            trainingFiles.remove(testFile);

            //            System.out.println("Training files: " + trainingFiles);
            System.out.println("Training files size: " + trainingFiles.size());
            System.out.println("Test file: " + testFile);

            runSingleFold(testFile, new ArrayList<File>(trainingFiles));

        }

    }

    public Set<SinglePrediction> runSingleFold(File testFile, List<File> trainingFiles)
            throws IOException
    {
        // concat the training files into a single file
        StringBuilder allTrainingFilesContent = new StringBuilder();
        for (File f : trainingFiles) {
            allTrainingFilesContent.append(FileUtils.readFileToString(f, "utf-8"));
        }

        // temporary training file from all training files
        File trainingFile = File
                .createTempFile("training", ".libsvm.txt");
        FileUtils.writeStringToFile(trainingFile, allTrainingFilesContent.toString(), "utf-8");

        File tempModelFile = File.createTempFile(testFile.getName(), ".model");

        System.out.println("Training...");

        // train
        trainSvm(trainingFile, tempModelFile);
        System.out.println("Done.");

        File tempModelPredictions = File.createTempFile("test_pred", ".txt");
        testSvm(testFile, tempModelFile, tempModelPredictions);

        // now load predictions and match them to the gold labels
        Set<SinglePrediction> predictions = loadPredictions(testFile, tempModelPredictions);

        System.out.println("Wrong predictions");
        for (SinglePrediction sp : predictions) {
            if (!sp.getGold().equals(sp.getPrediction())) {
                System.out.print(sp.getId() + ", ");
            }
        }
        System.out.println();

        //        System.out.println("Predictions: " + predictions);

        return predictions;
    }

    public Set<SinglePrediction> loadPredictions(File testFile, File modelPredictions)
            throws IOException
    {
        List<String> testLines = FileUtils.readLines(testFile);
        List<String> predictionLines = FileUtils.readLines(modelPredictions);

        if (testLines.size() != predictionLines.size()) {
            throw new IllegalStateException("Mismatch in test file size and prediction size. "
                    + "Test file " + testFile + " has " + testLines.size() + " lines while "
                    + "predictions " + modelPredictions + " has " + predictionLines.size()
                    + " lines");
        }

        Set<SinglePrediction> result = new HashSet<SinglePrediction>();

        for (int i = 0; i < testLines.size(); i++) {
            String test = testLines.get(i);
            String id = test.split("#")[1].trim();
            String gold = test.split("\t")[0].trim();
            String prediction = predictionLines.get(i).trim();

            SinglePrediction sp = new SinglePrediction(id, gold, prediction);

            result.add(sp);
        }

        return result;
    }

    public void trainSvm(File trainingFile, File tempModelFile)
            throws IOException
    {
        String options = "";

        String line = SVM_LIB_PATH + "svm-train " + options + trainingFile + " " + tempModelFile;

        runCommand(line);
    }


    public void testSvm(File testFile, File tempModelFile,
            File predictions)
            throws IOException
    {
        String options = "";

        String line = SVM_LIB_PATH + "svm-predict " + options + testFile + " " + tempModelFile + " "
                + predictions;

        runCommand(line);
    }

    public static void runCommand(String command)
            throws IOException
    {
        CommandLine cmdLine = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);

        // set one hour limit for training
        ExecuteWatchdog watchdog = new ExecuteWatchdog(1000 * 60 * 60);
        executor.setWatchdog(watchdog);

        System.out.println("Running\n" + command);

        int exitValue = executor.execute(cmdLine);
    }

    public static void main(String[] args)
            throws IOException
    {
        new SVMLibExperimentRunner().runCrossValidation(new File(args[0]));

    }
}
