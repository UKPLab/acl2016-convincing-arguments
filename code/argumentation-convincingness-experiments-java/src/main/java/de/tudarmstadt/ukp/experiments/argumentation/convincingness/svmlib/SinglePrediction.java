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

/**
 * @author Ivan Habernal
 */
public class SinglePrediction
        implements Comparable<SinglePrediction>
{
    private final String id;
    private final String gold;
    private final String prediction;

    public SinglePrediction(String id, String gold, String prediction)
    {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id is empty");
        }

        if (gold.isEmpty()) {
            throw new IllegalArgumentException("gold is empty");
        }

        if (prediction.isEmpty()) {
            throw new IllegalArgumentException("prediction is empty");
        }

        this.id = id;
        this.gold = gold;
        this.prediction = prediction;
    }

    public String getId()
    {
        return id;
    }

    public String getGold()
    {
        return gold;
    }

    public String getPrediction()
    {
        return prediction;
    }

    @Override public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SinglePrediction that = (SinglePrediction) o;

        return id.equals(that.id);

    }

    @Override public int hashCode()
    {
        return id.hashCode();
    }

    @Override public int compareTo(SinglePrediction o)
    {
        return this.id.compareTo(o.id);
    }

    @Override public String toString()
    {
        return "SinglePrediction{" +
                "id='" + id + '\'' +
                ", gold='" + gold + '\'' +
                ", prediction='" + prediction + '\'' +
                '}';
    }
}
