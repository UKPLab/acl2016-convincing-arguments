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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import edu.stanford.nlp.trees.Tree;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class DependencyTreeDepthFeature
        extends AbstractArgumentPairFeature
{
    public static final String FN_DEPENDENCY_TREE_DEPTH = "DependencyTreeDepth";

    @Override protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        List<Feature> featList = new ArrayList<Feature>();

        Collection<ROOT> root = JCasUtil.selectCovered(ROOT.class, paragraph);

        if (!root.isEmpty()) {
            Tree tree = TreeUtils.createStanfordTree(root.iterator().next());
            featList.add(new Feature(prefix + FN_DEPENDENCY_TREE_DEPTH, tree.depth()));
        }

        return featList;
    }
}
