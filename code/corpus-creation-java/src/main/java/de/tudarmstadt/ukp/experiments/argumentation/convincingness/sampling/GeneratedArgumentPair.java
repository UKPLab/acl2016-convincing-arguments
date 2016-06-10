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

/**
 * Equals and hashCode are only determined by arg1.id and arg2.id
 *
 * @author Ivan Habernal
 */
public class GeneratedArgumentPair
        extends AnnotatedArgumentPair
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ArgumentPair that = (ArgumentPair) o;

        if (!arg1.getId().equals(that.arg1.getId())) {
            return false;
        }
        return arg2.getId().equals(that.arg2.getId());

    }

    @Override
    public int hashCode()
    {
        int result = arg1.getId().hashCode();
        result = 31 * result + arg2.getId().hashCode();
        return result;
    }
}
