"""Bi-Directional LSTM
"""

from __future__ import print_function

import sys

import data_loader
import numpy as np
from theano.scalar import float32

from keras.preprocessing import sequence
from keras.utils.np_utils import accuracy
from keras.models import Graph
from keras.layers.core import Dense, Dropout
from keras.layers.embeddings import Embedding
from keras.layers.recurrent import LSTM

np.random.seed(1337)  # for reproducibility
max_features = 20000
max_len = 300  # cut texts after this number of words (among top max_features most common words)
batch_size = 32
nb_epoch = 5  # 5 epochs are meaningful to prevent over-fitting...

print('Loading data...')

# switch to my data
argv = sys.argv[1:]
input_folder = argv[0]
folds, word_index_to_embeddings_map = data_loader.load_my_data(input_folder, nb_words=max_features)

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

    print('Build model...')
    model = Graph()
    model.add_input(name='input', input_shape=(max_len,), dtype=int)
    # model.add_node(Embedding(max_features, 128, input_length=maxlen), name='embedding', input='input')
    model.add_node(Embedding(embeddings.shape[0], embeddings.shape[1], input_length=max_len, weights=[embeddings]),
                   name='embedding', input='input')
    model.add_node(LSTM(64), name='forward', input='embedding')
    model.add_node(LSTM(64, go_backwards=True), name='backward', input='embedding')
    model.add_node(Dropout(0.5), name='dropout', inputs=['forward', 'backward'])
    model.add_node(Dense(1, activation='sigmoid'), name='sigmoid', input='dropout')
    model.add_output(name='output', input='sigmoid')

    # try using different optimizers and different optimizer configs
    model.compile('adam', {'output': 'binary_crossentropy'})

    print('Train...')
    model.fit({'input': X_train, 'output': y_train}, batch_size=batch_size, nb_epoch=nb_epoch)

    print('Prediction')
    model_predict = model.predict({'input': X_test}, batch_size=batch_size)
    predicted_labels = np.round(np.array(model_predict['output']))

    # collect wrong predictions
    wrong_predictions_ids = []
    for i, (a, b) in enumerate(zip(y_test, predicted_labels)):
        if a != b:
            wrong_predictions_ids.append(ids_test[i])

    acc = accuracy(y_test, predicted_labels)
    print('Test accuracy:', acc)

    print('Wrong predictions:', wrong_predictions_ids)
