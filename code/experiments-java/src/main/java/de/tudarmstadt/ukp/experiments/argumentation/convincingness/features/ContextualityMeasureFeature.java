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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DKPro-TC developers (https://github.com/dkpro/dkpro-tc/)
 */
public class ContextualityMeasureFeature
        extends AbstractArgumentPairFeature
{
    @Override protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {

        List<Feature> result = new ArrayList<Feature>();

        double total = JCasUtil.selectCovered(POS.class, paragraph).size();
        double noun = JCasUtil.selectCovered(N.class, paragraph).size() / total;
        double adj = JCasUtil.selectCovered(ADJ.class, paragraph).size() / total;
        double prep = JCasUtil.selectCovered(PP.class, paragraph).size() / total;
        double art =
                JCasUtil.selectCovered(ART.class, paragraph).size() / total;// !includes determiners
        double pro = JCasUtil.selectCovered(PR.class, paragraph).size() / total;
        double verb = JCasUtil.selectCovered(V.class, paragraph).size() / total;
        double adv = JCasUtil.selectCovered(ADV.class, paragraph).size() / total;

        int interjCount = 0;
        for (POS tag : JCasUtil.selectCovered(O.class, paragraph)) {
            if (tag.getPosValue().contains("UH")) {
                interjCount++;
            }
        }
        double interj = interjCount / total;

        // noun freq + adj.freq. + prepositions freq. + article freq. - pronoun freq. - verb f. -
        // adverb - interjection + 100
        double contextualityMeasure =
                0.5 * (noun + adj + prep + art - pro - verb - adv - interj + 100);

        result.add(new Feature(prefix + "NounRate", noun));
        result.add(new Feature(prefix + "AdjectiveRate", adj));
        result.add(new Feature(prefix + "PrepositionRate", prep));
        result.add(new Feature(prefix + "ArticleRate", art));
        result.add(new Feature(prefix + "PronounRate", pro));
        result.add(new Feature(prefix + "VerbRate", verb));
        result.add(new Feature(prefix + "AdverbRate", adv));
        result.add(new Feature(prefix + "InterjectionRate", interj));
        result.add(new Feature(prefix + "CONTEXTUALITY_MEASURE_FN", contextualityMeasure));

        return result;
    }
}
