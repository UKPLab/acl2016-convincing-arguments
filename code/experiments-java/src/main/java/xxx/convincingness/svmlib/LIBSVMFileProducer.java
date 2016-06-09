/*
 * Copyright 2016 XXX
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

package xxx.convincingness.svmlib;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import xxx.convincingness.AdHocFeature;

import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author AUTHOR_HIDDEN
 */
public class LIBSVMFileProducer
        extends JCasAnnotator_ImplBase
{

    public static final String PARAM_FEATURES_TO_INT_MAPPING = "mappingFile";
    @ConfigurationParameter(name = PARAM_FEATURES_TO_INT_MAPPING, mandatory = true)
    protected File mappingFile;

    protected SortedMap<String, Integer> featuresToIntMapping;

    public static final String PARAM_OUTPUT_FILE = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
    protected File outputFile;

    protected PrintWriter printWriter;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            outputFile.getParentFile().mkdirs();

            printWriter = new PrintWriter(outputFile, "utf-8");

            featuresToIntMapping = (SortedMap<String, Integer>) new ObjectInputStream(
                    new FileInputStream(mappingFile)).readObject();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        printWriter.close();
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        // get the label first
        DocumentMetaData metaData = DocumentMetaData.get(aJCas);
        String[] split = metaData.getDocumentId().split("_");
        String label = split[split.length - 1];

        int outputLabel = "a1".equals(label) ? 0 : 1;

        SortedMap<Integer, Double> featureValues = new TreeMap<Integer, Double>();

        for (AdHocFeature feature : JCasUtil.select(aJCas, AdHocFeature.class)) {
            Integer key = featuresToIntMapping.get(feature.getName());
            Double value = feature.getValue();

            featureValues.put(key, value);
        }

        // now print it!
        printWriter.printf(Locale.ENGLISH, "%d\t", outputLabel);

        for (Map.Entry<Integer, Double> featureValue : featureValues.entrySet()) {
            printWriter.printf(Locale.ENGLISH, "%d:%.3f\t", featureValue.getKey(),
                    featureValue.getValue());
        }

        // add pair ID as comment
        printWriter.println("#" + metaData.getDocumentId());

    }
}
