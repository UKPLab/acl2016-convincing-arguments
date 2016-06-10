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
import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ivan Habernal
 */
public class SpellCheckingFeature
        extends AbstractArgumentPairFeature
{

    private Set<String> vocabulary;

    @Override public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            vocabulary = new TreeSet<String>();

            File wordFile = new File("/usr/share/dict/words");
            if (!wordFile.exists()) {
                throw new IOException("File not found " + wordFile);
            }

            // load vocabulary
            vocabulary.addAll(FileUtils.readLines(wordFile, "utf-8"));

            getLogger().info("Loaded " + vocabulary.size() + " words");
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
    {
        List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, paragraph);

        int oovWords = 0;

        for (Token token : tokens) {
            if (!vocabulary.contains(token.getCoveredText())) {
                oovWords++;
            }
        }

        return Collections.singletonList(new Feature(prefix + "oovWordsCount", oovWords));
    }
}
