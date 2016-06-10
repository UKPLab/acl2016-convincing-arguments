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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing  debates from {@code createdebate.com} site.
 *
 * @author Ivan Habernal
 */

public class CreateDebateComParser
        implements DebateParser
{

    @Override
    public Debate parseDebate(InputStream inputStream)
            throws IOException
    {
        Debate result = new Debate();

        Document doc = Jsoup.parse(inputStream, "UTF-8", "http://www.createdebate.com/");

        DebateMetaData debateMetaData = new DebateMetaData();
        result.setDebateMetaData(debateMetaData);

        // Set the Url of the doc
        debateMetaData.setUrl(doc.select("link").attr("href"));

        // title
        Element body = doc.body();
        Elements debateTitleElement = body.select("h1[class=debateTitle]");

        if (debateTitleElement.first() == null) {
            // not a debate
            return null;
        }

        String title = Utils.normalize(debateTitleElement.first().text());
        debateMetaData.setTitle(title);

        Element twoSidesAndScores = body
                .select("table[style=margin:0 auto;padding:0;border:0;text-align:center;width:98%;]")
                .first();

        if (twoSidesAndScores == null) {
            // this is not a two-side debate
            return null;
        }

        Elements twoSides = twoSidesAndScores.select("div[class=sideTitle]");
        if (twoSides.size() != 2) {
            // this is not a two-side debate
            return null;
        }

        // description
        StringBuilder debateDescriptionBuilder = new StringBuilder();
        Element description = body.select("#description").first();

        if (description != null) {
            Element descriptionText = description.select("div[class=centered debatelongDesc]")
                    .first();
            if (descriptionText.select("p").isEmpty()) {
                // just extract the text
                debateDescriptionBuilder.append(descriptionText.text());
            }
            else if ("".equals(descriptionText.select("p").first().text())) {
                // extract paragraphs
                for (Element p : descriptionText.select("p").select("span")) {
                    debateDescriptionBuilder.append(p.ownText());
                    debateDescriptionBuilder.append("\n");
                }
            }
            else {
                // extract paragraphs
                for (Element p : descriptionText.select("p")) {
                    debateDescriptionBuilder.append(p.text());
                    debateDescriptionBuilder.append("\n");
                }
            }
        }
        debateMetaData.setDescription(Utils.normalize(debateDescriptionBuilder.toString()));

        Element debateSideBoxL = body.select("div[class=debateSideBox sideL]").first();
        Element debateSideBoxR = body.select("div[class=debateSideBox sideR]").first();

        List<Element> debateSideBoxes = new ArrayList<>();
        debateSideBoxes.addAll(debateSideBoxL.select("div[class=argBox argument][id~=arg]"));
        debateSideBoxes.addAll(debateSideBoxR.select("div[class=argBox argument][id~=arg]"));

        for (Element argBody : debateSideBoxes) {
            Argument argumentWithParent = extractArgument(argBody);

            if (argumentWithParent != null) {
                Element parent = argBody.parent();
                Element previousElement = parent.previousElementSibling();
                String parentId = null;
                if (previousElement != null) {
                    Element realParent = previousElement.previousElementSibling();
                    if (realParent != null) {
                        parentId = realParent.id();
                    }
                }

                argumentWithParent.setParentId(parentId);

                result.getArgumentList().add(argumentWithParent);
            }
        }

        return result;
    }

    /**
     * Argument Box (from the {@link Element} class) Example of argument
     * box: <div class="argBox argument" id="arg153614">
     * The goal is to parse html debate pages from the  website http://www.createdebate.com
     * This method extracts argument from elements identified by
     * {@code <div class=argBox argument... } which correspond to one argument
     *
     * @param argBox element
     * @return argument or null, if argument's text is empty
     */
    public static Argument extractArgument(Element argBox)
    {
        Argument result = new Argument();

        Element name = argBox.select("a[href~=//www.createdebate.com/user/viewprofile/][title]")
                .first();
        result.setAuthor(name.ownText());

        Element argPoints = argBox.select("span[id~=tot.*]").first();
        result.setVoteUpCount(Integer.parseInt(argPoints.ownText()));

        // stance
        String stance = argBox.select("div.subtext").iterator().next().text()
                .replaceAll(".*Side: ", "").trim();
        result.setStance(stance);

        StringBuilder sb = new StringBuilder();
        Element argument = argBox.select("div[class=argBody]").first();

        String correctedHtml = Utils
                .normalizeWhitespaceAndRemoveUnicodeControlChars(argument.html());
        result.setOriginalHTML(correctedHtml);

        for (Element paragraphElement : argument.select("p")) {
            sb.append(paragraphElement.text());
            sb.append("\n");
        }

        String normalize = Utils.normalize(sb.toString());

        if (normalize.isEmpty()) {
            return null;
        }

        result.setText(normalize);

        result.setId(argBox.id());

        return result;
    }

}
