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

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.*;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class SVMLibExporter
{
    @SuppressWarnings("unchecked")
    public static SortedSet<String> extractFeatureNames(File inputDir)
            throws IOException, UIMAException, ClassNotFoundException
    {
        File tempFile = File.createTempFile("featureName", ".bin");
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(
                        BinaryCasReader.class,
                        BinaryCasReader.PARAM_SOURCE_LOCATION,
                        inputDir,
                        BinaryCasReader.PARAM_PATTERNS,
                        BinaryCasReader.INCLUDE_PREFIX + "*.bz2"
                ),
                AnalysisEngineFactory.createEngineDescription(
                        FeatureNameAggregator.class,
                        FeatureNameAggregator.PARAM_OUTPUT_FILE,
                        tempFile
                )
        );

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile));
        SortedSet<String> featureNames = (SortedSet<String>) ois.readObject();

        System.out.println("Features: " + featureNames.size());

        return featureNames;
    }

    public static SortedMap<String, Integer> mapFeaturesToInts(SortedSet<String> featureNames)
    {
        List<String> namesList = new ArrayList<String>(featureNames);

        SortedMap<String, Integer> result = new TreeMap<String, Integer>();

        for (int i = 0; i < namesList.size(); i++) {
            result.put(namesList.get(i), i);
        }

        return result;
    }

    public final static SortedSet<String> testFoldNames = new TreeSet<String>(
            Arrays.asList(
                    "ban-plastic-water-bottles_no-bad-for-the-economy",
                    "ban-plastic-water-bottles_yes-emergencies-only",
                    "christianity-or-atheism-_atheism",
                    "christianity-or-atheism-_christianity",
                    "evolution-vs-creation_creation",
                    "evolution-vs-creation_evolution",
                    "firefox-vs-internet-explorer_it-has-a-cute-logo-oh-and-extensions-err-add-ons",
                    "firefox-vs-internet-explorer_there-s-more-browsers-than-the-ie-firefox-is-an-animal-and-the-opera-isn-t-about-the-internet",
                    "gay-marriage-right-or-wrong_allowing-gay-marriage-is-right",
                    "gay-marriage-right-or-wrong_allowing-gay-marriage-is-wrong",
                    "human-growth-and-development-should-parents-use-spanking-as-an-option-to-discipline-_no",
                    "human-growth-and-development-should-parents-use-spanking-as-an-option-to-discipline-_yes",
                    "if-your-spouse-committed-murder-and-he-or-she-confided-in-you-would-you-turn-them-in-_no",
                    "if-your-spouse-committed-murder-and-he-or-she-confided-in-you-would-you-turn-them-in-_yes",
                    "india-has-the-potential-to-lead-the-world-_no-against",
                    "india-has-the-potential-to-lead-the-world-_yes-for",
                    "is-it-better-to-have-a-lousy-father-or-to-be-fatherless-_fatherless",
                    "is-it-better-to-have-a-lousy-father-or-to-be-fatherless-_lousy-father",
                    "is-porn-wrong-_no-is-is-not",
                    "is-porn-wrong-_yes-porn-is-wrong",
                    "is-the-school-uniform-a-good-or-bad-idea-_bad",
                    "is-the-school-uniform-a-good-or-bad-idea-_good",
                    "pro-choice-vs-pro-life_pro-choice",
                    "pro-choice-vs-pro-life_pro-life",
                    "should-physical-education-be-mandatory-in-schools-_no-",
                    "should-physical-education-be-mandatory-in-schools-_yes-",
                    "tv-is-better-than-books_books",
                    "tv-is-better-than-books_tv",
                    "which-type-of-endeavor-is-better-a-personal-pursuit-or-advancing-the-common-good-_advancing-the-common-good",
                    "which-type-of-endeavor-is-better-a-personal-pursuit-or-advancing-the-common-good-_personal-pursuit",
                    "william-farquhar-ought-to-be-honoured-as-the-rightful-founder-of-singapore_no-it-is-raffles-",
                    "william-farquhar-ought-to-be-honoured-as-the-rightful-founder-of-singapore_yes-of-course-"
            ));

    public static void main(String[] args)
            throws Exception
    {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        SortedSet<String> featureNames = extractFeatureNames(inputDir);

        SortedMap<String, Integer> mapping = mapFeaturesToInts(featureNames);

        String mappingFile = "/tmp/mapping.bin";
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(mappingFile));
        os.writeObject(mapping);
        os.close();

        int counter = 0;
        for (String foldName : testFoldNames) {
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
                            LIBSVMFileProducer.class,
                            LIBSVMFileProducer.PARAM_FEATURES_TO_INT_MAPPING,
                            mappingFile,
                            LIBSVMFileProducer.PARAM_OUTPUT_FILE,
                            new File(outputDir, foldName + ".libsvm.txt")
                    )

            );
        }
    }
}
