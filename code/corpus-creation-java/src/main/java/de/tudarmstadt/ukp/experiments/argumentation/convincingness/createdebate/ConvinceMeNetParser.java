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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for debates from {@code convinceme.net}.
 * <p/>
 * User-generated content (arguments) are licenced under Creative Commons Public Domain License
 * (see {@code www.convinceme.net/terms})
 * <p/>
 * @author Ivan Habernal
 */
public class ConvinceMeNetParser
        implements DebateParser
{

    @Override
    public Debate parseDebate(InputStream inputStream)
            throws IOException
    {
        Debate result = new Debate();

        Document doc = Jsoup.parse(inputStream, "UTF-8", "http://www.convinceme.net/");

        // debate title
        Elements header = doc.select("div.debate_header > h1, h2");

        Element h1 = header.iterator().next();

        // we need to remove sub-title (is weirdly generated as new line)
        Elements font = h1.select("font");

        if (font.isEmpty()) {
            // probably not a debate page
            return null;
        }
        font.iterator().next().remove();

        DebateMetaData debateMetaData = new DebateMetaData();
        result.setDebateMetaData(debateMetaData);

        // debate title text
        String title = Utils.normalize(h1.text());
        debateMetaData.setTitle(title);

        // url
        String url = doc.getElementsByAttributeValue("property", "og:url").iterator().next()
                .attr("content");
        debateMetaData.setUrl(url);

        // left side
        String leftStance = doc.select("div.left_side div.debate_side_top_name").text();
        Elements leftArguments = doc.select("div.left_side div.arguments > div");

        result.getArgumentList().addAll(extractArguments(leftArguments, leftStance));

        // right side
        String rightStance = doc.select("div.right_side div.debate_side_top_name").text();
        Elements rightArguments = doc.select("div.right_side div.arguments > div");

        result.getArgumentList().addAll(extractArguments(rightArguments, rightStance));

        return result;
    }

    /**
     * Extract all arguments from one side of the debate
     *
     * @param elements element
     * @param stance   side stance (known in advance)
     * @return list of arguments
     */
    protected static List<Argument> extractArguments(Elements elements, String stance)
    {
        List<Argument> result = new ArrayList<>();
        for (Element argElement : elements) {
            Argument argument = extractArgument(argElement);

            if (argument != null) {
                argument.setStance(stance);
                result.add(argument);
            }
        }

        return result;
    }

    /**
     * Extracts a signle argument
     *
     * @param argumentElement argument element
     * @return argument
     */
    protected static Argument extractArgument(Element argumentElement)
    {
        Argument result = new Argument();

        // ignore empty div
        if ("clear".equals(argumentElement.attr("class"))) {
            return null;
        }

        // author
        String author = argumentElement.select("div.argument_header a").iterator().next()
                .attributes().get("href");

        String convinced = ((TextNode) argumentElement
                .getElementsByAttributeValueStarting("id", "sway_").iterator().next().childNodes()
                .iterator().next()).text();

        // points earned
        Integer points = Integer.valueOf(convinced.split("\\s+")[0]);

        Element argumentBody = argumentElement.select("div.argument_body").iterator().next();

        StringBuilder argumentText = new StringBuilder();

        // parse argument text
        for (Node node : argumentBody.childNodes()) {
            if (node instanceof TextNode) {
                // text node
                argumentText.append(Utils.normalize(((TextNode) node).text()));
            }
            else if (node instanceof Element && "br".equals(((Element) node).tag().getName())) {
                // line break
                argumentText.append("\n");
            }
        }

        String text = argumentText.toString().replaceAll("\\n+", "\n").trim();

        // ignore empty arguments
        if (text.isEmpty()) {
            return null;
        }

        // id
        String id = argumentElement.attr("id").split("_")[1];

        // is it a rebuttal to some other argument?
        String parentId = null;
        Elements rebuttalTo = argumentBody.select("div.rebuttal_to");
        if (rebuttalTo.size() > 0) {
            Elements a = rebuttalTo.select("a");
            if (!a.isEmpty()) {
                String onclick = a.iterator().next().attr("onclick");

                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(onclick);
                if (m.find()) {
                    parentId = m.group();
                }
            }
        }

        result.setId(id);
        result.setParentId(parentId);
        result.setAuthor(author);
        result.setVoteUpCount(points);
        result.setText(text);

        return result;
    }
}
