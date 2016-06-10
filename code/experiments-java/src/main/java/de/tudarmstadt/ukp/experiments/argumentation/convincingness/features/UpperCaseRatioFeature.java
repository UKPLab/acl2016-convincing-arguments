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
import org.apache.uima.jcas.JCas;

import java.util.Collections;
import java.util.List;

/**
 * @author DKPro-TC developers (https://github.com/dkpro/dkpro-tc/)
 */
public class UpperCaseRatioFeature
        extends AbstractArgumentPairFeature
{
    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
    {
        String text = paragraph.getCoveredText();

        int upperCaseChars = 0;

        for (int i = 0; i < text.length(); i++) {
            upperCaseChars += Character.isUpperCase(text.charAt(i)) ? 1 : 0;
        }

        double value = upperCaseChars / text.length();

        return Collections.singletonList(new Feature(prefix + "upperCaseRatio", value));
    }
}
