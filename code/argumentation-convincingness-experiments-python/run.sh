#!/usr/bin/env bash
THEANO_FLAGS=mode=FAST_RUN,device=gpu,floatX=float32,optimizer_including=cudnn python bidirectional_lstm.py ../ConvArgStrict/
