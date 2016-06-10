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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Habernal
 */
public class Step6GraphTransitivityCleanerTest
{

    @Test
    public void testMergeClusters()
            throws Exception
    {
        Set<Set<String>> c1 = new HashSet<>();
        c1.add(new HashSet<>(Arrays.asList("1", "2")));
        c1.add(new HashSet<>(Arrays.asList("3", "4")));
        Set<Set<String>> merged1 = Step6GraphTransitivityCleaner.mergeClusters(c1);
        assertEquals(2, merged1.size());

        Set<Set<String>> c2 = new HashSet<>();
        c2.add(new HashSet<>(Arrays.asList("1", "2")));
        c2.add(new HashSet<>(Arrays.asList("3", "4")));
        c2.add(new HashSet<>(Arrays.asList("5", "4")));

        Set<Set<String>> merged2 = Step6GraphTransitivityCleaner.mergeClusters(c2);
        assertEquals(2, merged2.size());

        Set<Set<String>> c3 = new HashSet<>();
        c3.add(new HashSet<>(Arrays.asList("1", "5")));
        c3.add(new HashSet<>(Arrays.asList("3", "4")));
        c3.add(new HashSet<>(Arrays.asList("5", "4")));

        Set<Set<String>> merged3 = Step6GraphTransitivityCleaner.mergeClusters(c3);
        assertEquals(1, merged3.size());
    }
}