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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.regression;

import de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.SVMLibExperimentRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.SinglePrediction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan Habernal
 */
public class SVMLibRegressionExperimentRunner
        extends SVMLibExperimentRunner
{
    public static final String SVM_LIB_PATH = "/usr/local/bin/";

    @Override public void runCrossValidation(File dataDir)
            throws IOException
    {
        Set<SinglePrediction> allPredictions = new HashSet<SinglePrediction>();

        List<File> files = new ArrayList<File>(FileUtils
                .listFiles(dataDir, new String[] { "libsvm.txt" }, false));

        for (File testFile : files) {
            // create training files from the rest
            Set<File> trainingFiles = new HashSet<File>(files);
            trainingFiles.remove(testFile);

            //            System.out.println("Training files: " + trainingFiles);
            System.out.println("Training files size: " + trainingFiles.size());
            System.out.println("Test file: " + testFile);

            Set<SinglePrediction> predictions = runSingleFold(testFile,
                    new ArrayList<File>(trainingFiles));

            allPredictions.addAll(predictions);
        }

        List<SinglePrediction> predictions = new ArrayList<SinglePrediction>(allPredictions);

        double[][] matrix = new double[predictions.size()][];
        for (int i = 0; i < predictions.size(); i++) {
            SinglePrediction prediction = predictions.get(i);

            matrix[i] = new double[2];
            matrix[i][0] = Double.valueOf(prediction.getGold());
            matrix[i][1] = Double.valueOf(prediction.getPrediction());
        }

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(matrix);

        try {
            double pValue = pearsonsCorrelation.getCorrelationPValues().getEntry(0, 1);
            double correlation = pearsonsCorrelation.getCorrelationMatrix().getEntry(0, 1);

            System.out.println("Correlation: " + correlation);
            System.out.println("p-Value: " + pValue);
            System.out.println("Samples: " + predictions.size());

            SpearmansCorrelation sc = new SpearmansCorrelation(new Array2DRowRealMatrix(matrix));
            double pValSC = sc.getRankCorrelation().getCorrelationPValues().getEntry(0, 1);
            double corrSC = sc.getCorrelationMatrix().getEntry(0, 1);

            System.out.println("Spearman: " + corrSC + ", p-Val " + pValSC);
        }
        catch (MathException e) {
            throw new IOException(e);
        }

    }

    @Override
    public void trainSvm(File trainingFile, File tempModelFile)
            throws IOException
    {
        String options = " -s 4 ";

        String line = SVM_LIB_PATH + "svm-train " + options + trainingFile + " " + tempModelFile;

        runCommand(line);
    }

    public static void main(String[] args)
            throws IOException
    {
        new SVMLibRegressionExperimentRunner().runCrossValidation(new File(args[0]));
    }
}
