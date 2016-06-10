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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.ReadabilityAnnotator;
import de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * Five different readability measures
 *
 * @author DKPro-TC developers (https://github.com/dkpro/dkpro-tc/)
 */
public class ReadabilityScoreFeature
        extends AbstractArgumentPairFeature
{
    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        try {
            JCas paragraphCas = JCasFactory.createJCas();
            paragraphCas.setDocumentLanguage("en");
            paragraphCas.setDocumentText(jCas.getDocumentText());

            // re-annotate
            for (Sentence s : JCasUtil.selectCovered(Sentence.class, paragraph)) {
                Sentence newSentence = new Sentence(paragraphCas);
                newSentence.setBegin(s.getBegin());
                newSentence.setEnd(s.getEnd());
                newSentence.addToIndexes();
            }

            for (Token t : JCasUtil.selectCovered(Token.class, paragraph)) {
                Token newToken = new Token(paragraphCas);
                newToken.setBegin(t.getBegin());
                newToken.setEnd(t.getEnd());
                newToken.addToIndexes();
            }

            SimplePipeline.runPipeline(paragraphCas,
                    AnalysisEngineFactory.createEngineDescription(ReadabilityAnnotator.class)
            );

            List<Feature> result = new ArrayList<Feature>();

            for (ReadabilityScore rs : JCasUtil.select(paragraphCas, ReadabilityScore.class)) {
                String measureName = rs.getMeasureName();
                double score = rs.getScore();

                if (Double.isNaN(score)) {
                    score = 0;
                }

                result.add(new Feature(prefix + measureName, score));
            }

            return result;

        }
        catch (UIMAException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
