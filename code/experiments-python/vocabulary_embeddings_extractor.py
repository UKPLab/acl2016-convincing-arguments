import bz2
import cPickle
import unicodedata
from xml.etree import ElementTree

import numpy as np
import os
from nltk.tokenize.casual import TweetTokenizer


def tokenize(s):
    sentence_splitter = TweetTokenizer()
    tokens = sentence_splitter.tokenize(s)
    result = []
    for word in tokens:
        result.append(unicodedata.normalize('NFKD', word).encode('ascii', 'ignore'))

    return result


def load_word_frequencies_from_xml_corpus(files):
    """
    Reads all XML files with argument pairs and extracts dictionary with frequencies
    :param files:
    :return:
    """
    word_frequencies = dict()

    # parse each xml file in the directory
    for f in files:
        tree = ElementTree.parse(f)
        root = tree.getroot()

        # walk over all annotated argument pairs
        for annotated_argument_pair in root:
            tokens = tokenize(annotated_argument_pair.find('arg1').find('text').text)
            tokens.extend(tokenize(annotated_argument_pair.find('arg2').find('text').text))

            # update frequency
            for word in tokens:
                # add empty entry
                word_frequencies[word] = word_frequencies.get(word, 0) + 1

    return word_frequencies


def load_word_frequencies(files):
    """
    Loads vocabulary with frequencies from given documents sorted by frequency
    :param files: list of input files in the format label TAB arg1 TAG arg2
    """
    word_frequencies = dict()

    for single_file in files:
        for line in open(single_file):
            split = line.split("\t")
            # concat a1 and a2
            texts = split[1] + " " + split[2]
            # tokenize
            tokens = tokenize(texts)

            # update frequency
            for word in tokens:
                # add empty entry
                word_frequencies[word] = word_frequencies.get(word, 0) + 1

    # sort them by frequency
    # not required here!
    # sorted_word_frequencies = sorted(word_frequencies.items(), key=operator.itemgetter(1), reverse=True)

    return word_frequencies


def import_glove_line(line):
    partition = line.partition(' ')
    return partition[0], np.fromstring(partition[2], sep=' ')


def import_glove(filename, existing_vocabulary):
    word_map = dict()

    print(existing_vocabulary)

    with open(filename) as f:
        for line in f:
            head, vec = import_glove_line(line)
            if head in existing_vocabulary:
                word_map[head] = vec
                # print head
    return word_map  # where are the txt data


def prepare_embeddings(saved_embeddings):
    input_folder = "~/data2/convincingness/step2-argument-pairs/"
    frequencies = load_word_frequencies_from_xml_corpus(input_folder + x for x in os.listdir(input_folder))

    print(len(frequencies), "words")

    word_embedding_map = import_glove("~/data2/glove.840B.300d.txt", frequencies)

    with open(saved_embeddings, 'wb') as f:
        cPickle.dump((frequencies, word_embedding_map), f)

    print(frequencies)


def load_embeddings(saved_embeddings):
    """
    Loads frequencies (dict) and embeddings (dict) from pickled bz2 file
    :param saved_embeddings:  pkl.bz2 file
    :return: word_frequencies, word_embedding_map
    """
    (frequencies, word_embedding_map) = cPickle.load(bz2.BZ2File(saved_embeddings, 'r'))

    return frequencies, word_embedding_map


def dictionary_and_embeddings_to_indices(word_frequencies, embeddings):
    """
    Sort words by frequency, adds offset (3 items), maps word indices to embeddings and generate embeddings
    for padding, start of sequence, and OOV
    :param word_frequencies: dict (word: frequency)
    :param embeddings: dict (word: embeddings array)
    :return: word_to_indices_map (word: index), word_index_to_embeddings_map (index: embeddings)
    """

    # sort word frequencies from the most common ones
    sorted_word_frequencies_keys = sorted(word_frequencies, key=word_frequencies.get, reverse=True)

    word_to_indices_map = dict()
    word_index_to_embeddings_map = dict()

    # offset for all words so their indices don't start with 0
    # 0 is reserved for padding
    # 1 is reserved for start of sequence
    # 2 is reserved for OOV
    offset = 3

    # we also need to initialize embeddings for 0, 1, and 2
    # what is the dimension first?
    embedding_dimension = len(embeddings.values()[0])

    # for padding we add all zeros
    vector_padding = [0.0] * embedding_dimension

    # for start of sequence and OOV we add random vectors
    vector_start_of_sequence = 2 * 0.1 * np.random.rand(embedding_dimension) - 0.1
    vector_oov = 2 * 0.1 * np.random.rand(embedding_dimension) - 0.1

    # and add them to the embeddings map
    word_index_to_embeddings_map[0] = vector_padding
    word_index_to_embeddings_map[1] = vector_start_of_sequence
    word_index_to_embeddings_map[2] = vector_oov

    # iterate with index
    for idx, word in enumerate(sorted_word_frequencies_keys):
        # print idx, word

        new_index = idx + offset

        # update maps
        word_to_indices_map[word] = new_index

        if embeddings.get(word) is not None:
            word_index_to_embeddings_map[new_index] = embeddings.get(word)
        else:
            # fix embedding entries which are None with OOV vector
            word_index_to_embeddings_map[new_index] = vector_oov

    return word_to_indices_map, word_index_to_embeddings_map


def load_all(serialized_file='vocabulary.embeddings.all.pkl.bz2'):
    # load
    freq, embeddings_map = load_embeddings(serialized_file)

    # show first entry
    # print(freq.items()[0])
    # print(embeddings_map.items()[0])

    word_to_indices_map, word_index_to_embeddings_map = dictionary_and_embeddings_to_indices(freq, embeddings_map)

    return word_to_indices_map, word_index_to_embeddings_map
