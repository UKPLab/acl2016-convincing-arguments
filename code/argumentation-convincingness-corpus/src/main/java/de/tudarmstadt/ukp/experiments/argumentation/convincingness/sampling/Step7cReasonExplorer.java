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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step7cReasonExplorer
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws IOException
    {
        String inputDir = args[0];

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        // for generating ConvArgStrict use this
        String prefix = "no-eq_DescendingScoreArgumentPairListSorter";

        List<String> allReasons = new ArrayList<>();

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();

            if (!file.getName().startsWith(prefix)) {
                iterator.remove();
            }
        }

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);
            for (AnnotatedArgumentPair argumentPair : argumentPairs) {
                String goldLabel = argumentPair.getGoldLabel();

                // get gold reason statistics
                for (AnnotatedArgumentPair.MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                    String label = assignment.getValue();

                    if (goldLabel.equals(label)) {
                        allReasons.add(assignment.getReason());
                    }
                }
            }
        }

        // reproducibility
        Random r = new Random(1234);

        Collections.shuffle(allReasons, r);
        List<String> reasons = allReasons.subList(0, 200);
        Collections.sort(reasons);

        for (int i = 0; i < reasons.size(); i++) {
            System.out.printf(Locale.ENGLISH, "%d\t%s%n", i, reasons.get(i));
        }

    }
}
