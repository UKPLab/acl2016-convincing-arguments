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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.features;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.preprocessing.IdPrinterAnnotator;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * @author Ivan Habernal
 */
public class ExtractFeaturesPipeline
{

    public static void annotateFeaturesBaseline(File inputDir, File outputDir, boolean pairs)
            throws UIMAException, IOException
    {
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(
                        BinaryCasReader.class,
                        BinaryCasReader.PARAM_SOURCE_LOCATION,
                        inputDir,
                        BinaryCasReader.PARAM_PATTERNS,
                        BinaryCasReader.INCLUDE_PREFIX + "*.bz2"
                ),
                createEngineDescription(NGramPresenceFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),

                createEngineDescription(
                        BinaryCasWriter.class,
                        BinaryCasWriter.PARAM_TARGET_LOCATION,
                        outputDir,
                        BinaryCasWriter.PARAM_COMPRESSION,
                        CompressionMethod.BZIP2
                )
        );
    }

    public static void annotateFeatures(File inputDir, File outputDir, boolean pairs)
            throws UIMAException, IOException
    {
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(
                        BinaryCasReader.class,
                        BinaryCasReader.PARAM_SOURCE_LOCATION,
                        inputDir,
                        BinaryCasReader.PARAM_PATTERNS,
                        BinaryCasReader.INCLUDE_PREFIX + "*.bz2"
                ),
                createEngineDescription(IdPrinterAnnotator.class),
                createEngineDescription(AdjectiveEndingFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(ContextualityMeasureFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(DependencyTreeDepthFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(ExclamationFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(ModalVerbsFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(NamedEntityTypeCountFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(NGramPresenceFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(PastVsFutureFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(POSNGramFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(ProductionRulesFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(QuestionsRatioFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(ReadabilityScoreFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(SentimentFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(SpellCheckingFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(SuperlativeRatioFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(SurfaceFeatures.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),
                createEngineDescription(UpperCaseRatioFeature.class,
                        AbstractArgumentPairFeature.PARAM_IS_ARGUMENT_PAIR, pairs),

                createEngineDescription(
                        BinaryCasWriter.class,
                        BinaryCasWriter.PARAM_TARGET_LOCATION,
                        outputDir,
                        BinaryCasWriter.PARAM_COMPRESSION,
                        CompressionMethod.BZIP2
                )
        );
    }

    public static void main(String[] args)
            throws Exception
    {
        boolean pairs = args.length > 2 && "true".equals(args[2]);
//        annotateFeaturesBaseline(new File(args[0]), new File(args[1]), pairs);
        annotateFeatures(new File(args[0]), new File(args[1]), pairs);
    }
}
