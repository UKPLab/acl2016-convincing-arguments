# Which argument is more convincing? Analyzing and predicting convincingness of Web arguments using bidirectional LSTM

WORK IN PROGRESS

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

Don't hesitate to send me an e-mail or report an issue, if something is broken (and it shouldn't be) or if you have further questions.

For license information, see LICENSE files in `code/*` and `NOTICE.txt`.

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

Data are licensed under CC-BY-SA; the arguments originate from createdebate.com and convinceme.net (both licensed under CC-BY license).

## Requirements

* Java 1.7 and higher, Maven (for Java-based experiments)
* Python 2.7 and `virtualenv` (for Python-based experiments)
    * GPU is recommended but not required
* tested on 64-bit Linux versions

## Installation

TBD

## Running the experiments

TBD