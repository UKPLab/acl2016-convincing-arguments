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

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main class for extracting raw HTML debates from debates and storing them using the
 * internal format
 *
 * @author Ivan Habernal
 * @see Debate
 * @see DebateSerializer
 */
public class CorpusPreparator
{
    /**
     * Extracts all debates from raw HTML files in inFolder and stores them into outFolder
     * as serialized {@link Debate} objects (see {@link DebateSerializer}.
     *
     * @param inFolder     in folder
     * @param outFolder    out folder (must exist)
     * @param debateParser debate parser implementation
     * @throws IOException exception
     */
    public static void extractAllDebates(File inFolder, File outFolder, DebateParser debateParser)
            throws IOException
    {
        File[] files = inFolder.listFiles();
        if (files == null) {
            throw new IOException("No such dir: " + inFolder);
        }

        for (File f : files) {
            InputStream inputStream = new FileInputStream(f);
            try {
                Debate debate = debateParser.parseDebate(inputStream);

                // we ignore empty debates (without arguments)
                if (debate != null && !debate.getArgumentList().isEmpty()) {
                    // serialize to xml
                    String xml = DebateSerializer.serializeToXML(debate);

                    // same name with .xml
                    File outputFile = new File(outFolder, f.getName() + ".xml");

                    FileUtils.writeStringToFile(outputFile, xml, "utf-8");
                    System.out.println("Saved to " + outputFile.getAbsolutePath());

                    // ensure we can read it again
                    DebateSerializer.deserializeFromXML(FileUtils.readFileToString(outputFile));
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

    }

    public static void main(String[] args)
    {
        try {
            // args[0] = directory with exported html pages
            File inFolder = new File(args[0]);
            // args[1] = output directory
            File outFolder = new File(args[1]);

            outFolder.mkdir();

            // args[2] = parser implementation
            DebateParser parser = (DebateParser) Class.forName(args[2]).newInstance();

            extractAllDebates(inFolder, outFolder, parser);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
