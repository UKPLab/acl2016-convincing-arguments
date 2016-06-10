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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DKPro-TC developers (https://github.com/dkpro/dkpro-tc/)
 */
public class ModalVerbsFeature
        extends AbstractArgumentPairFeature
{
    public static final String FN_CAN = "ModalVerbCan";
    public static final String FN_COULD = "ModalVerbCould";
    public static final String FN_MIGHT = "ModalVerbMight";
    public static final String FN_MAY = "ModalVerbMay";
    public static final String FN_MUST = "ModalVerbMust";
    public static final String FN_SHOULD = "ModalVerbShould";
    public static final String FN_WILL = "ModalVerbWill";
    public static final String FN_WOULD = "ModalVerbWould";
    public static final String FN_SHALL = "ModalVerbShall";
    public static final String FN_ALL = "ModalVerbsRatio";
    public static final String FN_UNCERT = "ModalVerbsUncertain";

    @Override protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        int can = 0;
        int could = 0;
        int might = 0;
        int may = 0;
        int must = 0;
        int should = 0;
        int will = 0;
        int would = 0;
        int shall = 0;

        int n = 0;
        int modals = 0;
        for (V verb : JCasUtil.selectCovered(V.class, paragraph)) {
            n++;

            String text = verb.getCoveredText().toLowerCase();
            if (text.equals("can")) {
                can++;
                modals++;
            }
            else if (text.equals("could")) {
                could++;
                modals++;
            }
            else if (text.equals("might")) {
                might++;
                modals++;
            }
            else if (text.equals("may")) {
                may++;
                modals++;
            }
            else if (text.equals("must")) {
                must++;
                modals++;
            }
            else if (text.equals("should")) {
                should++;
                modals++;
            }
            else if (text.equals("will")) {
                will++;
                modals++;
            }
            else if (text.equals("would")) {
                would++;
                modals++;
            }
            else if (text.equals("shall")) {
                shall++;
                modals++;
            }

        }

        List<Feature> features = new ArrayList<Feature>();
        if (n > 0) {
            features.add(new Feature(prefix + FN_CAN, (double) can * 100 / n));
            features.add(new Feature(prefix + FN_COULD, (double) could * 100 / n));
            features.add(new Feature(prefix + FN_MIGHT, (double) might * 100 / n));
            features.add(new Feature(prefix + FN_MAY, (double) may * 100 / n));
            features.add(new Feature(prefix + FN_MUST, (double) must * 100 / n));
            features.add(new Feature(prefix + FN_SHOULD, (double) should * 100 / n));
            features.add(new Feature(prefix + FN_WILL, (double) will * 100 / n));
            features.add(new Feature(prefix + FN_WOULD, (double) would * 100 / n));
            features.add(new Feature(prefix + FN_SHALL, (double) shall * 100 / n));
            features.add(new Feature(prefix + FN_ALL, (double) modals * 100 / n));
            features.add(new Feature(prefix + FN_UNCERT, (double) (modals - will - must)
                    * 100 / n));
        }

        return features;
    }
}
