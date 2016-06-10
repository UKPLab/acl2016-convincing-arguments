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
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DKPro-TC developers (https://github.com/dkpro/dkpro-tc/)
 */
public class NamedEntityTypeCountFeature
        extends AbstractArgumentPairFeature
{
    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
    {
        List<NamedEntity> namedEntities = JCasUtil
                .selectCovered(jCas, NamedEntity.class, paragraph);

        FrequencyDistribution<String> distribution = new FrequencyDistribution<String>();

        for (NamedEntity ne : namedEntities) {
            String neType = ne.getType().getName();

            distribution.addSample(neType, 1);
        }

        List<Feature> result = new ArrayList<Feature>();

        for (String type : distribution.getKeys()) {
            result.add(new Feature(prefix + type, distribution.getCount(type)));
        }

        return result;
    }
}
