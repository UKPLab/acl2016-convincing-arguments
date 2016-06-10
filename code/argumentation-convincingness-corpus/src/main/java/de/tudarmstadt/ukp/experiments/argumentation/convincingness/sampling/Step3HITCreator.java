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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step3HITCreator
{
    private static final String MTURK_SANDBOX_URL = "https://workersandbox.mturk.com/mturk/externalSubmit";

    private static final String MTURK_ACUTAL_URL = "https://www.mturk.com/mturk/externalSubmit";

    /**
     * Parameter for output HIT files
     */
    private final static String fileNamePattern = "mthit-%05d.html";

    /**
     * How many arguments are shown in one HIT
     */
    private static final int ARGUMENTS_PER_HIT = 3;

    /**
     * Where the mustache template is stored (can also be classpath:xxx if packed in resources folder)
     */
    private final static String SOURCE_MUSTACHE_TEMPLATE = "mturk-template.mustache";

    /**
     * Use sandbox or real MTurk?
     */
    private final boolean sandbox;

    private Mustache mustache;

    File outputPath;

    private List<MTurkHITArgumentPairContainer.HITArgumentPair> argumentPairBuffer = new ArrayList<>();

    private int outputFileCounter = 0;

    public Step3HITCreator(boolean sandbox)
    {
        this.sandbox = sandbox;
    }

    public void initialize()
            throws IOException
    {
        InputStream stream = this.getClass().getClassLoader()
                .getResourceAsStream(SOURCE_MUSTACHE_TEMPLATE);
        if (stream == null) {
            throw new FileNotFoundException("Resource not found: " + SOURCE_MUSTACHE_TEMPLATE);
        }

        // compile template
        MustacheFactory mf = new DefaultMustacheFactory();
        Reader reader = new InputStreamReader(stream, "utf-8");
        mustache = mf.compile(reader, "template");

        // output path
        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }
    }

    public void process(ArgumentPair argumentPair)
            throws IOException
    {
        MTurkHITArgumentPairContainer.HITArgumentPair pojoArgumentPair = new MTurkHITArgumentPairContainer.HITArgumentPair();
        pojoArgumentPair.id = argumentPair.getId();
        if (!argumentPair.getArg1().getStance().equals(argumentPair.getArg2().getStance())) {
            throw new IllegalStateException(
                    "Stance of both arguments must be the same: " + argumentPair);
        }
        pojoArgumentPair.stance = argumentPair.getArg1().getStance();

        // split paragraphs
        pojoArgumentPair.arg1text = Arrays.asList(argumentPair.getArg1().getText().split("\n"));
        pojoArgumentPair.arg2text = Arrays.asList(argumentPair.getArg2().getText().split("\n"));

        pojoArgumentPair.debateDescription = argumentPair.getDebateMetaData().getDescription();
        pojoArgumentPair.debateTitle = argumentPair.getDebateMetaData().getTitle();

        argumentPairBuffer.add(pojoArgumentPair);

        if (argumentPairBuffer.size() >= ARGUMENTS_PER_HIT) {
            flushArgumentBufferToHIT();
        }
    }

    private void flushArgumentBufferToHIT()
            throws IOException
    {
        // fill some data first
        MTurkHITArgumentPairContainer tf = new MTurkHITArgumentPairContainer();

        tf.arguments.addAll(this.argumentPairBuffer);

        tf.numberOfArguments = tf.arguments.size();

        // make sure you use the proper type
        if (sandbox) {
            tf.mturkURL = MTURK_SANDBOX_URL;
        }
        else {
            tf.mturkURL = MTURK_ACUTAL_URL;
        }

        // get the correct output file
        File outputHITFile = new File(outputPath,
                String.format(Locale.ENGLISH, fileNamePattern, this.outputFileCounter));

        System.out.println("Generating " + outputHITFile);

        PrintWriter pw = new PrintWriter(outputHITFile);
        this.mustache.execute(pw, tf);
        IOUtils.closeQuietly(pw);

        // increase counter
        this.outputFileCounter++;

        // empty the current buffer
        this.argumentPairBuffer.clear();
    }

    public void collectionProcessComplete()
            throws IOException
    {
        // fill the rest of the buffer
        if (!argumentPairBuffer.isEmpty()) {
            flushArgumentBufferToHIT();
        }
    }

    /**
     * This is the division of 32 files with pairs into 4 batches
     */
    private static final SortedMap<String, SortedSet<String>> BATCHES = new TreeMap<>();

    static {
        BATCHES.put("11-first8topics-side1", new TreeSet<>(
                Arrays.asList("ban-plastic-water-bottles_no-bad-for-the-economy.xml",
                        "evolution-vs-creation_creation.xml",
                        "firefox-vs-internet-explorer_it-has-a-cute-logo-oh-and-extensions-err-add-ons.xml",
                        "gay-marriage-right-or-wrong_allowing-gay-marriage-is-right.xml",
                        "human-growth-and-development-should-parents-use-spanking-as-an-option-to-discipline-_no.xml",
                        "christianity-or-atheism-_atheism.xml",
                        "if-your-spouse-committed-murder-and-he-or-she-confided-in-you-would-you-turn-them-in-_no.xml",
                        "india-has-the-potential-to-lead-the-world-_no-against.xml")));

        BATCHES.put("12-second8topics-side1", new TreeSet<>(Arrays.asList(
                "is-it-better-to-have-a-lousy-father-or-to-be-fatherless-_fatherless.xml",
                "is-porn-wrong-_no-is-is-not.xml",
                "is-the-school-uniform-a-good-or-bad-idea-_bad.xml",
                "pro-choice-vs-pro-life_pro-choice.xml",
                "should-physical-education-be-mandatory-in-schools-_no-.xml",
                "tv-is-better-than-books_books.xml",
                "which-type-of-endeavor-is-better-a-personal-pursuit-or-advancing-the-common-good-_advancing-the-common-good.xml",
                "william-farquhar-ought-to-be-honoured-as-the-rightful-founder-of-singapore_no-it-is-raffles-.xml")));

        BATCHES.put("13-first8topics-side2", new TreeSet<>(
                Arrays.asList("ban-plastic-water-bottles_yes-emergencies-only.xml",
                        "evolution-vs-creation_evolution.xml",
                        "firefox-vs-internet-explorer_there-s-more-browsers-than-the-ie-firefox-is-an-animal-and-the-opera-isn-t-about-the-internet.xml",
                        "gay-marriage-right-or-wrong_allowing-gay-marriage-is-wrong.xml",
                        "human-growth-and-development-should-parents-use-spanking-as-an-option-to-discipline-_yes.xml",
                        "christianity-or-atheism-_christianity.xml",
                        "if-your-spouse-committed-murder-and-he-or-she-confided-in-you-would-you-turn-them-in-_yes.xml",
                        "india-has-the-potential-to-lead-the-world-_yes-for.xml")));

        BATCHES.put("14-second8topics-side2", new TreeSet<>(Arrays.asList(
                "is-it-better-to-have-a-lousy-father-or-to-be-fatherless-_lousy-father.xml",
                "is-porn-wrong-_yes-porn-is-wrong.xml",
                "is-the-school-uniform-a-good-or-bad-idea-_good.xml",
                "pro-choice-vs-pro-life_pro-life.xml",
                "should-physical-education-be-mandatory-in-schools-_yes-.xml",
                "tv-is-better-than-books_tv.xml",
                "which-type-of-endeavor-is-better-a-personal-pursuit-or-advancing-the-common-good-_personal-pursuit.xml",
                "william-farquhar-ought-to-be-honoured-as-the-rightful-founder-of-singapore_yes-of-course-.xml")));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws IOException
    {
        String inputDir = args[0];
        File outputDir = new File(args[1]);

        // sandbox or real MTurk?
        final boolean useSandbox = false;

        // required only for pilot
        // final int randomArgumentPairsCount = 50;

        // pseudo-random generator
        final Random random = new Random(1);

        for (Map.Entry<String, SortedSet<String>> entry : BATCHES.entrySet()) {
            Step3HITCreator hitCreator = new Step3HITCreator(useSandbox);
            hitCreator.outputPath = new File(outputDir, entry.getKey());
            hitCreator.initialize();

            // we will process only a subset first
            List<ArgumentPair> allArgumentPairs = new ArrayList<>();

            Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

            System.out.println(files);

            // read all files for the given batch
            for (File file : files) {
                if (entry.getValue().contains(file.getName())) {
                    allArgumentPairs
                            .addAll((List<ArgumentPair>) XStreamTools.getXStream().fromXML(file));
                }
            }

            // we have to shuffle them
            Collections.shuffle(allArgumentPairs, random);

            // only for pilot
            // List<ArgumentPair> selectedArgumentPairs = allArgumentPairs
            //      .subList(0, randomArgumentPairsCount);

            // for (ArgumentPair argumentPair : selectedArgumentPairs) {
            for (ArgumentPair argumentPair : allArgumentPairs) {
                hitCreator.process(argumentPair);
            }

            hitCreator.collectionProcessComplete();
        }
    }

}
