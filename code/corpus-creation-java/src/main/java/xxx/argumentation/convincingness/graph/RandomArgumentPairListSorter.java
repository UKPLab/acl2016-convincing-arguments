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

package xxx.argumentation.convincingness.graph;

import xxx.argumentation.convincingness.sampling.AnnotatedArgumentPair;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author AUTHOR_HIDDEN
 */
public class RandomArgumentPairListSorter
        implements ArgumentPairListSorter
{
    private final Random random;

    public RandomArgumentPairListSorter(int randomSeed)
    {
        this.random = new Random(randomSeed);
    }

    @Override
    public void sortArgumentPairs(List<AnnotatedArgumentPair> list)
    {
        Collections.shuffle(list, random);
    }
}
