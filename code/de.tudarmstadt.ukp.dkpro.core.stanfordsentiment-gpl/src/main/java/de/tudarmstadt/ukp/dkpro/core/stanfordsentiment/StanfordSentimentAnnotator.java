/*
 * Copyright 2014 Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tudarmstadt.ukp.dkpro.core.stanfordsentiment;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.ejml.simple.SimpleMatrix;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

/**
 * Experimental wrapper for {@link edu.stanford.nlp.pipeline.SentimentAnnotator} which assigns 5 scores to each sentence.
 * NOTE: Is very slow in the current state as it runs full Stanford pipeline and does not take into account any
 * existing DKPro annotations.
 *
 * @author Anil Narassiguing
 * @author Ivan Habernal
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = { "de.tudarmstadt.ukp.dkpro.core.stanfordsentiment.SentimentAnnotation" }
)
public class StanfordSentimentAnnotator
        extends JCasAnnotator_ImplBase
{

    private StanfordCoreNLP pipeline;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        //		props.setProperty("annotators", "sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        Collection<Sentence> dkproSentences = JCasUtil.select(jCas, Sentence.class);

        if (dkproSentences.isEmpty()) {
            throw new AnalysisEngineProcessException(
                    new IllegalStateException("No sentences annotated"));
        }

        for (Sentence sentenceDKPro : dkproSentences) {
            String sentenceText = sentenceDKPro.getCoveredText();

            Annotation annotation = pipeline.process(sentenceText);

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                SimpleMatrix sentimentCoefficients = RNNCoreAnnotations.getPredictions(tree);

                double veryNegative = sentimentCoefficients.get(0);
                double negative = sentimentCoefficients.get(1);
                double neutral = sentimentCoefficients.get(2);
                double positive = sentimentCoefficients.get(3);
                double veryPositive = sentimentCoefficients.get(4);

                StanfordSentimentAnnotation sentimentAnnotation = new StanfordSentimentAnnotation(
                        jCas);
                sentimentAnnotation.setBegin(sentenceDKPro.getBegin());
                sentimentAnnotation.setEnd(sentenceDKPro.getEnd());
                sentimentAnnotation.setVeryNegative(veryNegative);
                sentimentAnnotation.setNegative(negative);
                sentimentAnnotation.setNeutral(neutral);
                sentimentAnnotation.setPositive(positive);
                sentimentAnnotation.setVeryPositive(veryPositive);
                sentimentAnnotation.addToIndexes();
            }
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(
                        BinaryCasReader.class,
                        BinaryCasReader.PARAM_SOURCE_LOCATION, inputDir,
                        BinaryCasReader.PARAM_PATTERNS,
                        BinaryCasReader.INCLUDE_PREFIX + "*.bz2"
                ),

                // Sentiment
                AnalysisEngineFactory.createEngineDescription(StanfordSentimentAnnotator.class),

                AnalysisEngineFactory.createEngineDescription(
                        BinaryCasWriter.class,
                        BinaryCasWriter.PARAM_TARGET_LOCATION,
                        outputDir,
                        BinaryCasWriter.PARAM_COMPRESSION,
                        CompressionMethod.BZIP2
                )
        );
    }

}
