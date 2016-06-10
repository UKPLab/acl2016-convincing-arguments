"""Bi-Directional LSTM
"""

from __future__ import print_function

import sys

import data_loader_regression
import numpy as np
from keras.layers.core import Dense, Dropout
from keras.layers.embeddings import Embedding
from keras.layers.recurrent import LSTM
from keras.models import Graph
from keras.preprocessing import sequence
from theano.scalar import float32
from scipy import stats


def print_correlations(x_np_array, y_np_array):
    sp = stats.spearmanr(x_np_array, y_np_array)
    print("Spearman correlation:", sp.correlation)
    print("p-value:", sp.pvalue)
    pr = stats.pearsonr(x_np_array, y_np_array)
    print("Pearson correlation:", pr[0])
    print("p-value:", pr[1])


np.random.seed(1337)  # for reproducibility
max_features = 20000
max_len = 150  # cut texts after this number of words (among top max_features most common words)
batch_size = 32
nb_epoch = 5  # 5 epochs are meaningful to prevent over-fitting...

print('Loading data...')

# switch to my data
argv = sys.argv[1:]
input_folder = argv[0]
folds, word_index_to_embeddings_map = data_loader_regression.load_my_data(input_folder, nb_words=max_features)

# collect all gold scores and all predictions to compute final correlations
all_gold_scores = np.array([])
all_prediction_scores = np.array([])

# print statistics
for fold in folds.keys():
    print("Fold name ", fold)
    X_train, y_train, ids_train = folds.get(fold)["training"]
    X_test, y_test, ids_test = folds.get(fold)["test"]

    # print(type(ids_test))
    # print(ids_test)

    # converting embeddings to numpy 2d array: shape = (vocabulary_size, 300)
    embeddings = np.asarray([np.array(x, dtype=float32) for x in word_index_to_embeddings_map.values()])

    print(len(X_train), 'train sequences')
    print(len(X_test), 'test sequences')

    print("Pad sequences (samples x time)")
    X_train = sequence.pad_sequences(X_train, maxlen=max_len)
    X_test = sequence.pad_sequences(X_test, maxlen=max_len)
    print('X_train shape:', X_train.shape)
    print('X_test shape:', X_test.shape)
    y_train = np.array(y_train)
    y_test = np.array(y_test)

    # look at the data first
    # print(y_test)

    print('Build model...')
    model = Graph()
    model.add_input(name='input', input_shape=(max_len,), dtype=int)
    # model.add_node(Embedding(max_features, 128, input_length=maxlen), name='embedding', input='input')
    model.add_node(Embedding(embeddings.shape[0], embeddings.shape[1], input_length=max_len, weights=[embeddings]),
                   name='embedding', input='input')
    model.add_node(LSTM(64), name='forward', input='embedding')
    model.add_node(LSTM(64, go_backwards=True), name='backward', input='embedding')
    model.add_node(Dropout(0.5), name='dropout', inputs=['forward', 'backward'])

    # match output layer for regression better
    model.add_node(Dense(1, activation='linear', init='uniform'), name='output_layer', input='dropout')
    model.add_output(name='output', input='output_layer')

    # use mean absolute error loss
    model.compile('adam', {'output': 'mean_absolute_error'})

    print('Train...')
    model.fit({'input': X_train, 'output': y_train}, batch_size=batch_size, nb_epoch=nb_epoch)

    print('Prediction')
    model_predict = model.predict({'input': X_test}, batch_size=batch_size)
    # print(model_predict)
    output_predictions = np.asarray(model_predict['output']).flatten()

    # print(output_predictions)

    # collect results
    all_gold_scores = np.append(all_gold_scores, y_test)
    all_prediction_scores = np.append(all_prediction_scores, output_predictions)

    # print fold correlations
    print_correlations(output_predictions, y_test)


# print final correlations
print_correlations(all_gold_scores, all_prediction_scores)

