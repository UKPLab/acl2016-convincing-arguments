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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.preprocessing;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.io.ArgumentPairReader;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.io.RankedArgumentReader;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;

/**
 * @author Ivan Habernal
 */
public class Pipeline
{
    private static AnalysisEngineDescription createPipeline(String outDir)
            throws ResourceInitializationException
    {
        return AnalysisEngineFactory.createEngineDescription(
                AnalysisEngineFactory.createEngineDescription(
                        ParagraphSplitter.class,
                        ParagraphSplitter.PARAM_SPLIT_PATTERN,
                        ParagraphSplitter.SINGLE_LINE_BREAKS_PATTERN
                ),
                AnalysisEngineFactory.createEngineDescription(
                        // only on existing WebParagraph annotations
                        StanfordSegmenter.class,
                        StanfordSegmenter.PARAM_ZONE_TYPES,
                        Paragraph.class.getCanonicalName()
                ),
                AnalysisEngineFactory.createEngineDescription(
                        StanfordLemmatizer.class
                ),
                AnalysisEngineFactory.createEngineDescription(
                        StanfordPosTagger.class
                ),
                AnalysisEngineFactory.createEngineDescription(
                        StanfordParser.class
                ),
                AnalysisEngineFactory.createEngineDescription(
                        StanfordNamedEntityRecognizer.class
                ),
                AnalysisEngineFactory.createEngineDescription(
                        BinaryCasWriter.class,
                        BinaryCasWriter.PARAM_TARGET_LOCATION,
                        outDir,
                        BinaryCasWriter.PARAM_COMPRESSION,
                        CompressionMethod.BZIP2
                )
        );
    }

    public static void processDataPairs(String inputDir, String outputDir)
            throws IOException, UIMAException
    {
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(
                        ArgumentPairReader.class,
                        ArgumentPairReader.PARAM_SOURCE_LOCATION,
                        inputDir,
                        ArgumentPairReader.PARAM_PATTERNS,
                        ArgumentPairReader.INCLUDE_PREFIX + "*.csv"
                ),
                createPipeline(outputDir)
        );
    }

    public static void processDataRank(String inputDir, String outputDir)
            throws IOException, UIMAException
    {
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(
                        RankedArgumentReader.class,
                        RankedArgumentReader.PARAM_SOURCE_LOCATION,
                        inputDir,
                        RankedArgumentReader.PARAM_PATTERNS,
                        RankedArgumentReader.INCLUDE_PREFIX + "*.csv"
                ),
                createPipeline(outputDir)
        );
    }

    public static void main(String[] args)
            throws IOException, UIMAException
    {
        //        processDataPairs(args[0], args[1]);
        processDataRank(args[0], args[1]);
    }
}
