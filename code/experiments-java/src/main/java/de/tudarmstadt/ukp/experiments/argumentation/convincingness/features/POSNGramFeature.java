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

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class POSNGramFeature
        extends AbstractArgumentPairFeature
{
    public static final String FEATURE_NAME = "_pos_ngram_";

    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
    {
        List<Feature> result = new ArrayList<Feature>();
        // extract post n-grams
        FrequencyDistribution<String> documentPOSNGrams = getSentencePosNGrams(jCas, 1, 3, true,
                paragraph);

        for (String posNGram : documentPOSNGrams.getKeys()) {
            // we use sparse vectors here
            result.add(new Feature(prefix + FEATURE_NAME + posNGram,
                    documentPOSNGrams.getCount(posNGram)));
        }
        return result;
    }

    public static FrequencyDistribution<String> getSentencePosNGrams(JCas jcas, int minN, int maxN,
            boolean useCanonical, Paragraph paragraph)
    {
        FrequencyDistribution<String> result = new FrequencyDistribution<String>();

        List<String> posTagString = new ArrayList<String>();
        for (POS p : JCasUtil.selectCovered(jcas, POS.class, paragraph)) {
            if (useCanonical) {
                posTagString.add(p.getClass().getSimpleName());
            }
            else {
                posTagString.add(p.getPosValue());
            }
        }
        String[] posAsArray = posTagString.toArray(new String[posTagString.size()]);

        for (List<String> nGram : new NGramStringListIterable(posAsArray, minN, maxN)) {
            result.inc(StringUtils.join(nGram, "_"));

        }

        return result;
    }

}
