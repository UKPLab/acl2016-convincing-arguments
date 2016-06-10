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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.io;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Ivan Habernal
 */
public class RankedArgumentReader
        extends ResourceCollectionReaderBase
{
    private Queue<String> currentLines = new ArrayDeque<String>();

    String currentFileName = null;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException
    {
        super.initialize(aContext);
    }

    @Override
    public void getNext(CAS aCAS)
            throws IOException, CollectionException
    {
        try {
            if (currentLines.isEmpty()) {
                loadNextFile();
            }

            String line = currentLines.poll();
            String[] split = line.split("\t");
            String id = split[0].trim();
            String score = split[1].trim();
            String argument = split[2].trim();

            // set label
            JCas jCas = aCAS.getJCas();
            DocumentMetaData metaData = DocumentMetaData.create(jCas);
            String name = currentFileName + "_" + id + "_" + score;
            metaData.setDocumentTitle(name);
            metaData.setDocumentId(name);

            // set content
            jCas.setDocumentText(argument);
            jCas.setDocumentLanguage("en");
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    protected void loadNextFile()
            throws IOException
    {
        Resource res = nextFile();
        currentFileName = res.getResource().getFile().getName().replace(".csv", "");
        currentLines.addAll(IOUtils.readLines(res.getInputStream(), "utf-8"));

        // remove the first line (comment)
        currentLines.poll();
    }

    @Override public boolean hasNext()
            throws IOException, CollectionException
    {
        return !currentLines.isEmpty() || super.hasNext();
    }
}
