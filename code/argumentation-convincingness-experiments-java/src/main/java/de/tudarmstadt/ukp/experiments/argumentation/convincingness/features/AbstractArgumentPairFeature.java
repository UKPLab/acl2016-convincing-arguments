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
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.AdHocFeature;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public abstract class AbstractArgumentPairFeature
        extends JCasAnnotator_ImplBase
{
    public static final String PARAM_IS_ARGUMENT_PAIR = "argumentPair";
    @ConfigurationParameter(name = PARAM_IS_ARGUMENT_PAIR, defaultValue = "true")
    boolean argumentPair;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
    }

    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {

        List<Paragraph> paragraphs = new ArrayList<Paragraph>(
                JCasUtil.select(jCas, Paragraph.class));

        List<Feature> result = new ArrayList<Feature>();

        if (argumentPair) {
            if (paragraphs.size() != 2) {
                throw new IllegalStateException(
                        "Two paragraph annotations expected but " + paragraphs.size() + " found");
            }
            result.addAll(extractFeaturesFromArgument(jCas, paragraphs.get(0), "a1"));
            result.addAll(extractFeaturesFromArgument(jCas, paragraphs.get(1), "a2"));
        } else {
            // only one paragraph
            result.addAll(extractFeaturesFromArgument(jCas, paragraphs.get(0), "a1"));
        }

        // and now add all features to the annotations
        for (Feature f : result) {
            AdHocFeature feature = new AdHocFeature(jCas);
            feature.setBegin(0);
            feature.setEnd(1);
            feature.setName(f.getName());
            Object object = f.getValue();

            if (object != null) {
                if (object instanceof Double) {
                    feature.setValue((Double) object);
                }
                else if (object instanceof Integer) {
                    feature.setValue((double) ((Integer) object));
                }
                else if (object instanceof Long) {
                    feature.setValue((double) ((Long) object));
                }
                else {
                    throw new IllegalStateException("Unknown value: " + object.getClass());
                }

                feature.addToIndexes();
            }

        }
    }

    /**
     * Extract features for each argument from argument pair and adds a prefix to the feature
     * names
     *
     * @param jCas      jcas with argument pair
     * @param paragraph paragraph covering the given argument (a1 or a2)
     * @param prefix    output feature prefix
     * @return list of features
     */
    protected abstract List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException;
}
