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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * Features: sentence length (tokens, characters), count and ratio of words with more than 6 letters
 * <p/>
 * From "Stance Classification of Ideological Debates: Data, Models, Features, and Constraints"
 * Kazi Saidul Hasan and Vincent Ng
 * Inspired from Anand et al.'s (2011) features
 *
 */
public class SurfaceFeatures
        extends AbstractArgumentPairFeature
{
    @Override protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
    {

        // Post length in letters
        double postLength = paragraph.getCoveredText().length();

        // Number of word per sentences
        List<Token> tokens = JCasUtil.selectCovered(Token.class, paragraph);
        double wordPerSentence = tokens.size();

        // Ratio of words with more than 6 letters
        double nbMoreSixLetters = 0;

        for (Token token : tokens) {
            if (token.getCoveredText().length() > 6) {
                nbMoreSixLetters++;
            }
        }

        Double sixLettersWordsRatio = null;
        if (wordPerSentence > 0 && nbMoreSixLetters > 0) {
            sixLettersWordsRatio = nbMoreSixLetters / wordPerSentence;
        }

        List<Feature> featList = new ArrayList<Feature>();
        featList.add(new Feature(prefix + "sentence_length", postLength));
        featList.add(new Feature(prefix + "word_per_sentence", wordPerSentence));
        featList.add(new Feature(prefix + "word_more_6_letters", nbMoreSixLetters));
        featList.add(
                new Feature(prefix + "word_more_6_letters_ratio", sixLettersWordsRatio));
        return featList;
    }
}
