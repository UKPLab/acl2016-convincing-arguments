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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.AdHocFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Collects all feature names
 */
public class FeatureNameAggregator
        extends JCasAnnotator_ImplBase
{
    SortedSet<String> featureNames = new TreeSet<String>();

    public static final String PARAM_OUTPUT_FILE = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
    File outputFile;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        for (AdHocFeature feature : JCasUtil.select(aJCas, AdHocFeature.class)) {
            featureNames.add(feature.getName());
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(outputFile));
            os.writeObject(featureNames);
            os.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
