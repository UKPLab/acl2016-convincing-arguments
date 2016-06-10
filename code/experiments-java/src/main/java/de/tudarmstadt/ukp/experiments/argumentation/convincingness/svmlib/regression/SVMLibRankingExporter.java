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

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.LIBSVMFileProducer;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.SVMLibExporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author Ivan Habernal
 */
public class SVMLibRankingExporter
{

    public static void main(String[] args)
            throws Exception
    {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        SortedSet<String> featureNames = SVMLibExporter.extractFeatureNames(inputDir);

        SortedMap<String, Integer> mapping = SVMLibExporter.mapFeaturesToInts(featureNames);

        String mappingFile = "/tmp/mapping.bin";
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(mappingFile));
        os.writeObject(mapping);
        os.close();

        int counter = 0;
        for (String foldName : SVMLibExporter.testFoldNames) {
            counter++;

            System.out.println("Processing " + foldName);

            // generate training data
            SimplePipeline.runPipeline(
                    CollectionReaderFactory.createReaderDescription(

                            BinaryCasReader.class,
                            BinaryCasReader.PARAM_SOURCE_LOCATION,
                            inputDir,
                            BinaryCasReader.PARAM_PATTERNS,
                            BinaryCasReader.INCLUDE_PREFIX + foldName + "*.bz2"
                    ),
                    AnalysisEngineFactory.createEngineDescription(
                            LIBSVMRegressionFileProducer.class,
                            LIBSVMFileProducer.PARAM_FEATURES_TO_INT_MAPPING,
                            mappingFile,
                            LIBSVMFileProducer.PARAM_OUTPUT_FILE,
                            new File(outputDir, foldName + ".libsvm.txt")
                    )

            );
        }
    }
}
