# Which argument is more convincing? Analyzing and predicting convincingness of Web arguments using bidirectional LSTM

Source code, data, and supplementary materials for our ACL 2016 article. Please use the following citation:

```
@inproceedings{Habernal.Gurevych.2016.ACL,
  author    = {Ivan Habernal and Iryna Gurevych},
  title     = {{Which argument is more convincing? Analyzing and predicting convincingness
               of Web arguments using bidirectional LSTM}},
  booktitle = {Proceedings of the 54th Annual Meeting of the Association for Computational
               Linguistics (ACL 2016)},
  volume    = {Volume 1: Long Papers},
  year      = {2016},
  address   = {Berlin, Germany},
  pages     = {(to appear)},
  publisher = {Association for Computational Linguistics},
  url       = {https://www.ukp.tu-darmstadt.de/publications/details/?tx_bibtex_pi1[pub_id]=TUD-CS-2016-0104}
}
```

> **Abstract:** We propose a new task in the field of computational argumentation in which we
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


* **Contact person:** Ivan Habernal, habernal@ukp.informatik.tu-darmstadt.de
    * UKP Lab: http://www.ukp.tu-darmstadt.de/
    * TU Darmstadt: http://www.tu-darmstadt.de/

Drop me a line or report an issue if something is broken (and shouldn't be) or if you have any questions.

For license information, see LICENSE files in `code/*/` and `NOTICE.txt`.

> This repository contains experimental software and is published for the sole purpose of giving additional background details on the respective publication. 

## Project structure

* `code` &mdash; Experimental source codes
* `data` &mdash; *UKPConvArg1* corpus

## Data description

The `data` directory contains the following sub-folders

* `UKPConvArg1-full-XML`
    * This is the full corpus as referred in the article (Table 2, UKPConvArgAll). It contains 32 xml files, each file corresponding to one debate/side. Total number of argument pairs is 16,081. 
* `UKPConvArg1-Ranking-CSV`
    * Exported tab-delimited file with 1,052 arguments with their ID, rank score, and text (Table 2, UKPConvArgRank)
* `UKPConvArg1Strict-XML`
    * Cleaned version used for experiments in the article (Table 2, UKPConvArgSctrict). It contains 11,650 argument pairs in 32 XML files.
* `UKPConvArg1Strict-CSV`
    * The same as `UKPConvArg1Strict-XML` but exported into tab-delimited CSV with ID, more convincing argument label (a1 or a2) and both arguments (a1, tab, a2)

The data are licensed under <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">CC-BY (Creative Commons Attribution 4.0 International License)</a>. 

The source arguments originate from
* [createdebate.com](http://www.createdebate.com) licensed under [CC-BY](http://creativecommons.org/licenses/by/3.0/)
* [convinceme.net](http://convinceme.net) licensed under [Creative Commons Public Domain License](https://creativecommons.org/licenses/publicdomain/)

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a>

### Data formats

#### XML

Both `UKPConvArg1-full-XML` and `UKPConvArg1Strict-XML` have the same XML format. Here is a commented excerpt from the `evolution-vs-creation_evolution.xml` file.

```XML
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
```

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
    * Pair ID (`firstArgumentID_secondArgumentID`)
    * Gold label (which one is more convincing) &mdash; `a1` or `a2`
    * Text of argument 1
    * Text of argument 2
        * Line breaks are encoded as `<br/>`

## Experiments

### Requirements

* Java 1.7 and higher, Maven (for Java-based experiments)
* Python 2.7 and `virtualenv` (for Python-based experiments)
    * GPU is recommended but not required
* Tested on 64-bit Linux versions

### Deep-learning experiments

* Create and activate a new Python virtual environment (Python 2.7 used as the default interpreter)
```bash
$ cd code/argumentation-convincingness-experiments-python
$ virtualenv env
New python executable in env/bin/python
Installing setuptools, pip...done.
$ source env/bin/activate
(env)user@x:~/acl2016-convincing-arguments/code/argumentation-convincingness-experiments-python$
```

* Install requirements (might take a few minutes)
```bash
$ python env/bin/pip install -r requirements.txt 
Downloading/unpacking git+git://github.com/Theano/Theano.git@4e7f550 (from -r requirements.txt (line 4))
...
(lots of Fortran and C warnings because of SciPy and NumPy)
...
```
* You should have CUDA installed on your machine for GPU-enabled computation
    * Refer to http://deeplearning.net/software/theano/install.html
    * This might get sometimes a bit tricky to install

* Running the first experiment for argument pair prediction
```bash
$ THEANO_FLAGS=mode=FAST_RUN,device=gpu,floatX=float32,optimizer_including=cudnn \
python bidirectional_lstm.py ../../data/UKPConvArg1Strict-CSV/
```

which outputs
    
```
Using Theano backend.
Using gpu device 0: GRID K520 (CNMeM is disabled, CuDNN 4007)
/home/ubuntu/devel/acl2016-submission/env/local/lib/python2.7/site-packages/theano/tensor/signal/downsample.py:5: UserWarning: downsample module has been moved to the pool module.
  warnings.warn("downsample module has been moved to the pool module.")
Loading data...
Loaded 32 files
Fold name  which-type-of-endeavor-is-better-a-personal-pursuit-or-advancing-the-common-good-_personal-pursuit.csv
11296 train sequences
354 test sequences
Pad sequences (samples x time)
X_train shape: (11296, 300)
X_test shape: (354, 300)
Build model...
Train...
Epoch 1/5
11296/11296 [==============================] - 142s - loss: 0.5745     
Epoch 2/5
11296/11296 [==============================] - 142s - loss: 0.3129     
Epoch 3/5
11296/11296 [==============================] - 142s - loss: 0.2240     
Epoch 4/5
11296/11296 [==============================] - 142s - loss: 0.1708     
Epoch 5/5
11296/11296 [==============================] - 142s - loss: 0.1386     
Prediction
Test accuracy: 0.683615819209
Wrong predictions: ['arg33053_arg33125', 'arg33070_arg33121', 'arg33101_arg33115', ...
Fold name  gay-marriage-right-or-wrong_allowing-gay-marriage-is-right.csv
11246 train sequences
404 test sequences
...
```

* Accuracy is reported for each test fold name along with wrong predictions for error analysis.
    * Corresponds to Table 3 (0.68 for the row "Personal pursuit or common good? Personal" in this case)
    * Warning: The numbers produced might be slightly different from those in the paper if a different
    version of Theano is used. Theano underwent heavy development changes back in February
    which unfortunately influences the results.
    I tried to fix it by installing a certain commit of Theano (see the requirements.txt file),
    but it still doesn't really match the version I used for the paper experiments. Lesson learned:
    *never* install current master branch (aka development version) from git using `git+git://github.com/Theano/Theano.git` (although this is a suggested installation of
    "bleeding edge" Theano for Keras!) Always use either release version or a certain commit, e.g.,
     `git+git://github.com/Theano/Theano.git@rel-0.8.2`. 

* Running the second experiment for argument ranking
```bash
$ THEANO_FLAGS=mode=FAST_RUN,device=gpu,floatX=float32,optimizer_including=cudnn \
python bidirectional_lstm_regression.py ../../data/UKPConvArg1-Ranking-CSV/
```


### SVM-based experiments

* Install `LIBSVM` ( https://www.csie.ntu.edu.tw/~cjlin/libsvm/ version used: Version 3.21, Dec 2015)
    * Add `svm-train` and `svm-predict` to `/usr/local/bin/`
    * Alternatively, adjust the path constant in `SVMLibExperimentRunner`
* Compile the Java project
    * `$ cd code/`
    * `$ mvn package`
    * `$ cd argumentation-convincingness-experiments-java/target`
    
#### Preparing data for SVM

We need to annotate the data with all linguistic features and then export into SVMLIB format.
 
* Run `Pipeline` from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.preprocessing`; parameters:
    * `data/UKPConvArg1Strict-CSV` (gold standard)
    * `/some/temp/folder/strict1` (temporary output)
    * `data/UKPConvArg1-Ranking-CSV` (gold standard)
    * `/some/temp/folder/ranking1` (temporary output)

* Run `StanfordSentimentAnnotator` from `de.tudarmstadt.ukp.dkpro.core.stanfordsentiment`; parameters:
    * `/some/temp/folder/strict1` (temporary output from the previous step)
    * `/some/temp/folder/strict2` (temporary output)
    * Analogically, run in again with `/some/temp/folder/ranking1 /some/temp/folder/ranking2`
    
* Run `ExtractFeaturesPipeline` from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.features`; parameters
    * `/some/temp/folder/strict2` (temporary output from the previous step)
    * `/some/temp/folder/strict3` (temporary output)
    * `true` (this parameter says we need argument pairs)
    * Analogically, for ranking: `/some/temp/folder/ranking2 /some/temp/folder/ranking3 false`
    
* Run `SVMLibExporter` from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib`; parameters
    * `/some/temp/folder/strict3` (temporary output from the previous step)
    * `/some/temp/folder/UKPConvArg1-Strict-libsvm` (output in SVMLIB format)
    * Analogically, `/some/temp/folder/ranking3 /some/temp/folder/UKPConvArg1-Ranking-libsvm` for ranking using `LIBSVMRegressionFileProducer`
    
#### Running the experiments

* Run `SVMLibExperimentRunner` from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib`
    * Parameter: `/some/temp/folder/UKPConvArg1-Strict-libsvm` (output from the previous step)
    
Output:
```
user@ubuntu:~/acl2016-convincing-arguments/code/argumentation-convincingness-experiments-java$ java -cp target/argumentation-convincingness-experiments-java-1.0-SNAPSHOT.jar:target/lib/* de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.SVMLibExperimentRunner /tmp/UKPConvArg1-Strict-libsvm
Training files size: 31
Test file: /tmp/UKPConvArg1-Strict-libsvm/christianity-or-atheism-_christianity.libsvm.txt
Training...
Running
/usr/local/bin/svm-train /tmp/training6947586292572191419.libsvm.txt /tmp/christianity-or-atheism-_christianity.libsvm.txt478591432284640112.model
....
WARNING: using -h 0 may be faster
*.*
optimization finished, #iter = 5663
nu = 0.476407
obj = -5087.572013, rho = 0.072998
nSV = 5612, nBSV = 5244
Total nSV = 5612
Done.
Running
/usr/local/bin/svm-predict /tmp/UKPConvArg1-Strict-libsvm/christianity-or-atheism-_christianity.libsvm.txt /tmp/christianity-or-atheism-_christianity.libsvm.txt478591432284640112.model /tmp/test_pred5567075616977838561.txt
Accuracy = 67.8161% (177/261) (classification)
Wrong predictions
christianity-or-atheism-_christianity_arg230311_arg230910_a1, [...] 
[...]
```
* It outputs accuracy for each test fold (corresponds to Table 3) as well as wrong predictions for error analysis

Ranking runs analogically using `SVMLibRegressionExperimentRunner` from `de.tudarmstadt.ukp.experiments.argumentation.convincingness.svmlib.regression`