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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.regression;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.LIBSVMFileProducer;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.AdHocFeature;

import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Ivan Habernal
 */
public class LIBSVMRegressionFileProducer
        extends LIBSVMFileProducer
{

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        // get the label first
        DocumentMetaData metaData = DocumentMetaData.get(aJCas);
        String[] split = metaData.getDocumentId().split("_");
        String scoreString = split[split.length - 1];

        SortedMap<Integer, Double> featureValues = new TreeMap<Integer, Double>();

        for (AdHocFeature feature : JCasUtil.select(aJCas, AdHocFeature.class)) {
            Integer key = featuresToIntMapping.get(feature.getName());
            Double value = feature.getValue();

            featureValues.put(key, value);
        }

        // now print it!
        printWriter.printf(Locale.ENGLISH, "%s\t", scoreString);

        for (Map.Entry<Integer, Double> featureValue : featureValues.entrySet()) {
            printWriter.printf(Locale.ENGLISH, "%d:%.3f\t", featureValue.getKey(),
                    featureValue.getValue());
        }

        // add pair ID as comment
        printWriter.println("#" + metaData.getDocumentId());
    }
}
