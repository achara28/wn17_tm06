import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
import warnings
import os.path
import os
import sys
import posixpath as psp

from tensorflow.contrib import learn
from tensorflow.contrib.learn.python import SKCompat as skflow
from sklearn.metrics import mean_squared_error
from hdfs3 import HDFileSystem
from lstm_predictor2 import lstm_model, load_csvdata,rnn_model,gru_model
filename = "ptp_resultsRNNall.txt"
results = open(filename, 'a')

warnings.filterwarnings("ignore")
working_dir = "/user/canast02/ystr=2016"
from hdfs import InsecureClient
hdfs = InsecureClient(url='http://pythia1.in.cs.ucy.ac.cy:50070', user='canast02')
fnames=hdfs.list(working_dir)

print(fnames)



LOG_DIR = 'resources/logs/'
TIMESTEPS = 1
RNN_LAYERS = [{'num_units': 16}, {'num_units': 16}]
DENSE_LAYERS = []
TRAINING_STEPS = 1000
PRINT_STEPS = TRAINING_STEPS  # / 10
BATCH_SIZE = 64

regressor = skflow(learn.Estimator(model_fn=rnn_model(TIMESTEPS, RNN_LAYERS, DENSE_LAYERS)))
#   model_dir=LOG_DIR)

# 201512200045
dateparse = lambda dates: pd.datetime.strptime(dates, '%Y%m%d%H%M')
#working_dir = "C:/Users/Andreas/Desktop/nms/ystr=2016/ymstr=1/ymdstr=24"

rawdata = None

df_list = []
for root, dirs, files in hdfs.walk(working_dir):
    file_list = []

    for filename in files:
        if filename.endswith('.csv'):
            file_list.append(psp.join(root, filename))
   # print(file_list)
    for file in file_list:
        print(file)

        #df = pd.read_csv(file)

        with hdfs.read(file, encoding='utf-8') as reader:
            df = pd.read_csv(reader, delimiter="|", usecols=[1, 2, 6, 7], header=None, na_values=["NIL"],
                              na_filter=True, names=["meas_info", "counter", "value", "time"], index_col='time')
            #df = df[df["counter"] == int(sys.argv[1])]
            df = df[df["counter"] == 67179779]
            #        print(df[["value"]])
            df_list.append(df[["value"]])

if df_list:
    rawdata = pd.concat(df_list)

#print(rawdata)
# rawdata = pd.read_csv("./input/fakehdfs/nms/ystr=2015/ymstr=12/ymdstr=20/hive_0_201512200030.csv", delimiter="|",
#                      usecols=[7], header=None)

print(len(rawdata))

X, y = load_csvdata(rawdata, TIMESTEPS, seperate=False)

# noise_train = np.asmatrix(np.random.normal(0, 0.2, len(y['train'])), dtype=np.float32)
# noise_val = np.asmatrix(np.random.normal(0, 0.2, len(y['val'])), dtype=np.float32)
# noise_test = np.asmatrix(np.random.normal(0, 0.2, len(y['test'])), dtype=np.float32)  # asmatrix

# noise_train = np.transpose(noise_train)
# noise_val = np.transpose(noise_val)
# noise_test = np.transpose(noise_test)

# y['train'] = np.add(y['train'], noise_train)
# y['val'] = np.add(y['val'], noise_val)
# y['test'] = np.add(y['test'], noise_test)

# print(type(y['train']))


print('-----------------------------------------')
print('train y shape', y['train'].shape)
print('train y shape_num', y['train'][1:5])
# print('noise_train shape', noise_train.shape)
# print('noise_train shape_num', noise_train.shape[1:5])

# create a lstm instance and validation monitor
validation_monitor = learn.monitors.ValidationMonitor(X['val'], y['val'], )
# every_n_steps=PRINT_STEPS,)
# early_stopping_rounds=1000)
# print(X['train'])
# print(y['train'])

skflow(regressor.fit(X['train'], y['train'],
                     monitors=[validation_monitor],
                     batch_size=BATCH_SIZE,
                     steps=TRAINING_STEPS))

print('X train shape', X['train'].shape)
print('y train shape', y['train'].shape)

print('X test shape', X['test'].shape)
print('y test shape', y['test'].shape)
predicted = np.asmatrix(regressor.predict(X['test']), dtype=np.float32)  # ,as_iterable=False))
predicted = np.transpose(predicted)

rmse = np.sqrt((np.asarray((np.subtract(predicted, y['test']))) ** 2).mean())

# this previous code for rmse was incorrect, array and not matricies was needed: rmse = np.sqrt(((predicted - y[
# 'test']) ** 2).mean())
score = mean_squared_error(predicted, y['test'])
nmse = score / np.var(
    y['test'])  # should be variance of original data and not data from fitted model, worth to double check

results.write("RMSE: %f \n" % rmse)
print("NSME: %f" % nmse)
print("MSE: %f" % score)
results.flush()
results.close()

plot_test, = plt.plot(y['test'], label='test')
plot_predicted, = plt.plot(predicted, label='predicted')
plt.legend(handles=[plot_predicted, plot_test])
plt.show()
