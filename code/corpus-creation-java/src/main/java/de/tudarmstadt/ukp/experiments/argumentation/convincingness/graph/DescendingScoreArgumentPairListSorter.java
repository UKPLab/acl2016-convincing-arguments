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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.graph;

import de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling.AnnotatedArgumentPair;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling.Step6GraphTransitivityCleaner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class DescendingScoreArgumentPairListSorter
        implements ArgumentPairListSorter
{

    @Override
    public void sortArgumentPairs(List<AnnotatedArgumentPair> list)
    {
        Collections.sort(list, new Comparator<AnnotatedArgumentPair>()
        {
            @Override
            public int compare(AnnotatedArgumentPair o1, AnnotatedArgumentPair o2)
            {
                return Double.compare(Step6GraphTransitivityCleaner
                                .computeEdgeWeight(o2, Step6GraphTransitivityCleaner.LAMBDA_PENALTY),
                        Step6GraphTransitivityCleaner.computeEdgeWeight(o1,
                                Step6GraphTransitivityCleaner.LAMBDA_PENALTY));
            }
        });
    }
}
