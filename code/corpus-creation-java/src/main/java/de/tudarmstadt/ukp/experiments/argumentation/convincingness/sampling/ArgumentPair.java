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

import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.DebateMetaData;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Argument;

/**
 * @author Ivan Habernal
 */
public class ArgumentPair
{
    protected String id;
    protected Argument arg1;
    protected Argument arg2;
    protected DebateMetaData debateMetaData;

    public ArgumentPair(ArgumentPair argumentPair)
    {
        this.id = argumentPair.id;
        this.arg1 = argumentPair.arg1;
        this.arg2 = argumentPair.arg2;
        this.debateMetaData = argumentPair.debateMetaData;
    }

    public ArgumentPair()
    {
        
    }

    public Argument getArg1()
    {
        return arg1;
    }

    public void setArg1(Argument arg1)
    {
        this.arg1 = arg1;
    }

    public Argument getArg2()
    {
        return arg2;
    }

    public void setArg2(Argument arg2)
    {
        this.arg2 = arg2;
    }

    public DebateMetaData getDebateMetaData()
    {
        return debateMetaData;
    }

    public void setDebateMetaData(DebateMetaData debateMetaData)
    {
        this.debateMetaData = debateMetaData;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
