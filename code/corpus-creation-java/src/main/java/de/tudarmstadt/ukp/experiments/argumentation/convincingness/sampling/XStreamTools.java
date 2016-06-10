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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Argument;
import de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate.Debate;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Ivan Habernal
 */
public class XStreamTools
{
    private static XStream xStream;

    public static String toXML(Object object)
            throws IOException
    {
        Writer out = new StringWriter();
        out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        XStream xStream = getXStream();

        xStream.toXML(object, out);
        IOUtils.closeQuietly(out);

        return out.toString();
    }

    public static Object fromXML(String xml)
            throws IOException
    {
        XStream xStream = getXStream();
        return xStream.fromXML(new StringReader(xml));
    }

    public static XStream getXStream()
    {
        if (xStream == null) {
            xStream = new XStream(new StaxDriver());
            xStream.alias("argument", Argument.class);
            xStream.alias("debate", Debate.class);
            xStream.alias("argumentPair", ArgumentPair.class);
            xStream.alias("annotatedArgumentPair", AnnotatedArgumentPair.class);
            xStream.alias("mTurkAssignment", AnnotatedArgumentPair.MTurkAssignment.class);

            // no references for duplicate objects
            xStream.setMode(XStream.NO_REFERENCES);
        }

        return xStream;
    }

    public static void toXML(Object object, File outputFile)
            throws IOException
    {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        getXStream().toXML(object, outputStream);
        IOUtils.closeQuietly(outputStream);
    }
}
