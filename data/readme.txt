# UKPConvArg1 Corpus Readme

ACL 2016 Article: "Which argument is more convincing? Analyzing and predicting convincingness
of Web arguments using bidirectional LSTM"

(Latest change: 2016-08-02)

Please use the following citation:

@inproceedings{Habernal.Gurevych.2016.ACL,
  author    = {Ivan Habernal and Iryna Gurevych},
  title     = {{Which argument is more convincing? Analyzing and predicting convincingness
               of Web arguments using bidirectional LSTM}},
  booktitle = {Proceedings of the 54th Annual Meeting of the Association for Computational
              Linguistics (Volume 1: Long Papers)},
  year      = {2016},
  address   = {Berlin, Germany},
  pages     = {1589--1599},
  publisher = {Association for Computational Linguistics},
  url       = {http://www.aclweb.org/anthology/P16-1150}
}

Abstract: We propose a new task in the field of computational argumentation in which we
investigate qualitative properties of Web arguments, namely their convincingness. We cast the
problem as relation classification, where a pair of arguments having the same stance to the same
prompt is judged. We annotate a large datasets of 16k pairs of arguments over 32 topics and
investigate whether the relation "A is more convincing than B" exhibits properties of total
ordering; these findings are used as global constraints for cleaning the crowdsourced data.
We propose two tasks: (1) predicting which argument from an argument pair is more convincing and
(2) ranking all arguments to the topic based on their convincingness. We experiment with
feature-rich SVM and bidirectional LSTM and obtain 0.76-0.78 accuracy and 0.35-0.40 Spearman's
correlation in a cross-topic evaluation. We release the newly created corpus UKPConvArg1 and the
experimental software under open licenses.

Contact person: Ivan Habernal, habernal@ukp.informatik.tu-darmstadt.de
    * UKP Lab: http://www.ukp.tu-darmstadt.de/
    * TU Darmstadt: http://www.tu-darmstadt.de/

See the related source codes at GitHub: https://github.com/UKPLab/acl2016-convincing-arguments

## Data description

The `data` directory contains the following sub-folders

* `UKPConvArg1-full-XML`
    * This is the full corpus as referred in the article (Table 2, UKPConvArgAll). It contains
    32 xml files, each file corresponding to one debate/side. Total number of argument pairs
    is 16,081.
* `UKPConvArg1-Ranking-CSV`
    * Exported tab-delimited file with 1,052 arguments with their ID, rank score, and text
    (Table 2, UKPConvArgRank)
* `UKPConvArg1Strict-XML`
    * Cleaned version used for experiments in the article (Table 2, UKPConvArgSctrict). It
    contains 11,650 argument pairs in 32 XML files.
* `UKPConvArg1Strict-CSV`
    * The same as `UKPConvArg1Strict-XML` but exported into tab-delimited CSV with ID, more
    convincing argument label (a1 or a2) and both arguments (a1, tab, a2)

The data are licensed under CC-BY (Creative Commons Attribution 4.0 International License).

The source arguments originate from
* http://www.createdebate.com licensed under CC-BY (http://creativecommons.org/licenses/by/3.0/)
* http://convinceme.net licensed under Creative Commons Public Domain License
  (https://creativecommons.org/licenses/publicdomain/)

### Data formats

#### XML

Both `UKPConvArg1-full-XML` and `UKPConvArg1Strict-XML` have the same XML format. Here is a
commented excerpt from the `evolution-vs-creation_evolution.xml` file.

<?xml version="1.0"?>
<list>                                                                     <!-- the root element -->
  <annotatedArgumentPair>                             <!-- is a list of annotated argument pairs -->
    <id>803_794</id>                                <!-- id of the first and the second argument -->
    <arg1>                                            <!-- first argument with original metadata -->
      <author>http://www.convinceme.net/profile/318/seanohagan.html</author>
      <voteUpCount>0</voteUpCount>
      <voteDownCount>0</voteDownCount>
      <stance>Evolution</stance>
      <text>I have to contradict phro and say that the peppered moths do show evidence of evolution.
       The data may have been insufficient, but evolution did occur.
       When different alleles are expressed due to external factors, this is evolution.</text>
      <id>803</id>
      <!-- some files also have "originalHTML" element with the text in escaped HTML -->
    </arg1>
    <arg2>                                           <!-- second argument with original metadata -->
      <author>http://www.convinceme.net/profile/633/yolei36.html</author>
      <voteUpCount>0</voteUpCount>
      <voteDownCount>0</voteDownCount>
      <stance>Evolution</stance>
      <text>You can actually see evolution happen. Fruit Flies are quite useful for this experiment
        since the breed, live, and die so quickly. You have to understand evolution happens because
        of mutations and they survive because those mutations have made it easier for the creature
        to survive then the others. ie. natural selection. Speciation is also an example of
        evolution... different species come about because they have adapted to a slightly different
        enviroment. If you look you can see evolution</text>
      <id>794</id>
    </arg2>
    <debateMetaData>                                              <!-- metadata about the debate -->
      <title>Evolution vs. Creation</title>
      <url>http://www.convinceme.net/debates/90/Evolution-vs-Creation.html</url>
      <!-- some debates also have a textual "description" element -->
    </debateMetaData>
    <mTurkAssignments>                              <!-- five Amazon Mechanical Turk assignments -->
      <mTurkAssignment>
        <turkID>A365TVEXXLHT2U</turkID>
        <hitID>311HQEI8RSS2Z8RYBJEC3U2W0M5Z78</hitID>
        <assignmentAcceptTime>2016-02-22 21:40:51.0 UTC</assignmentAcceptTime>
        <assignmentSubmitTime>2016-02-22 21:46:33.0 UTC</assignmentSubmitTime>
        <!-- value selected by the worker; "a1" means argument 1 is more convincing, "a2" means -->
        <!-- argument 2 is more convincing, "equal" means they are equally convincing -->
        <!-- (note that equal does not occur in the "Strict" corpus) -->
        <value>a2</value>
        <!-- Explanation written by the worker -->
        <reason>A2 provides a more in depth explanation of how to identify evolution.</reason>
        <assignmentId>3K772S5NP9N43M37I7RDXHMSEWHEHU</assignmentId>
        <turkRank>432</turkRank>                                <!-- overall rank of this worker -->
        <turkCompetence>0.9971903929844271</turkCompetence>   <!-- worker's competence from MACE -->
        <workerStance>none</workerStance>    <!-- worker's stance; "opposite", "same", or "none" -->
      </mTurkAssignment>
      <mTurkAssignment>
        <turkID>A1LNZS1KNSREZB</turkID>
        <hitID>311HQEI8RSS2Z8RYBJEC3U2W0M5Z78</hitID>
        <!-- and so on... -->
      </mTurkAssignment>
      <!-- and so on... -->
    </mTurkAssignments>
    <!-- estimated gold label for this pair (which arg is more convincing) -->
    <goldLabel>a2</goldLabel>
  </annotatedArgumentPair>
  <!-- and so on... -->
</list>


The XML structure can be easily accessed by the corresponding Java classes, such as

* `DebateMetaData`
* `Argument`

from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.createdebate`

and

* `AnnotatedArgumentPair`
* `MTurkAssignment`

from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling`.

In fact, each single file is a serialized `List` of `AnnotatedArgumentPair` instances using XStream.
The whole corpus can be thus loaded by

```Java
Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

for (File file : files) {
    List<AnnotatedArgumentPair> argumentPairs =
        (List<AnnotatedArgumentPair>) XStreamTools.getXStream().fromXML(file);
    // do whatever you want
}
```

See for example `Step7aLearningDataProducer` in `de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling`.

#### CSV

CSV files are generated from XML files by `Step7aLearningDataProducer`. An example from `evolution-vs-creation_evolution.csv`:

```
#id	label	a1	a2
778_80854	a2	Life has been around for ...	Science can prove that ... <br/> The big ...
...
```

* The first line is a comment
* Each line is then a single argument pair, tab-separated
    * Pair ID (first argument ID, second argument ID)
    * Gold label (which one is more convincing)
    * Text of argument 1
    * Text of argument 2
        * Line breaks are encoded as `<br/>`
