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
import de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * @author Ivan Habernal
 */
public class SentimentFeature
        extends AbstractArgumentPairFeature
{
    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        List<Feature> result = new ArrayList<Feature>();
        List<StanfordSentimentAnnotation> sentimentAnnotations = selectCovered(
                StanfordSentimentAnnotation.class, paragraph);
        if (sentimentAnnotations.size() != 1) {
            getLogger().warn("No sentiment annotations for sentence " + paragraph.getCoveredText());
        }
        else {
            StanfordSentimentAnnotation sentiment = sentimentAnnotations.get(0);

            result.add(new Feature(prefix + "sentimentVeryNegative",
                    sentiment.getVeryNegative()));
            result.add(new Feature(prefix + "sentimentNegative", sentiment.getNegative()));
            result.add(new Feature(prefix + "sentimentNeutral", sentiment.getNeutral()));
            result.add(new Feature(prefix + "sentimentPositive", sentiment.getPositive()));
            result.add(new Feature(prefix + "sentimentVeryPositive",
                    sentiment.getVeryPositive()));
        }
        return result;

    }
}
