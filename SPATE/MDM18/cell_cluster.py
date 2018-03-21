import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
import warnings
import os.path
import os

from tensorflow.contrib import learn
from tensorflow.contrib.learn.python import SKCompat as skflow
from sklearn.metrics import mean_squared_error

from lstm_predictor import generate_data, load_csvdata, lstm_model

working_dir = "./input/fakehdfs/nms/"
cells = None


def learningpercell(cell_name):
    warnings.filterwarnings("ignore")

    LOG_DIR = 'resources/logs/'
    TIMESTEPS = 100
    RNN_LAYERS = [{'num_units': 256}, {'num_units': 256}]
    DENSE_LAYERS = [32]
    TRAINING_STEPS = 100
    PRINT_STEPS = TRAINING_STEPS  # / 10
    BATCH_SIZE = 64

    regressor = skflow(learn.Estimator(model_fn=lstm_model(TIMESTEPS, RNN_LAYERS, DENSE_LAYERS)))
    #   model_dir=LOG_DIR)

    # 201512200045
    dateparse = lambda dates: pd.datetime.strptime(dates, '%Y%m%d%H%M')
    rawdata = None
    df_list = []
    for root, dirs, files in os.walk(working_dir):
        file_list = []

        for filename in files:
            if filename.endswith('.csv'):
                file_list.append(os.path.join(root, filename))
        for file in file_list:
            df = pd.read_csv(file, delimiter="|", usecols=[1, 2, 5, 6, 7], header=None, na_values=["NIL"],
                             na_filter=True, names=["meas_info", "counter", "cellname", "value", "time"],
                             index_col='time')
            # df = df[df["counter"] == 67179778]
            df = df[df["cellname"].str.contains(cell_name)]
            # df.drop('cellname', axis=1, inplace=True)
            # df = df[df["counter"] == 50331671]
            #        print(df[["value"]])
            if not df.empty:
                df_list.append(df[["value"]])

    if df_list:
        rawdata = pd.concat(df_list)
    #        else:
    #            return
    print(len(rawdata))
    if len(rawdata) <= 0:
        print("Den ftanni")
        return

        # rawdata = pd.read_csv("./input/fakehdfs/nms/ystr=2015/ymstr=12/ymdstr=20/hive_0_201512200030.csv", delimiter="|",
    #                      usecols=[7], header=None)

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

    print("RSME: %f" % rmse)
    print("NSME: %f" % nmse)
    print("MSE: %f" % score)

    plot_test, = plt.plot(y['test'], label='test')
    plot_predicted, = plt.plot(predicted, label='predicted')
    plt.legend(handles=[plot_predicted, plot_test])
    plt.show()


# for root, dirs, files in os.walk(working_dir):
#     file_list = []
#
#     for filename in files:
#         if filename.endswith('.csv'):
#             file_list.append(os.path.join(root, filename))
#     df_cells_list = []
#     for file in file_list:
#         df_cells = pd.read_csv(file, delimiter="|", usecols=[5, 7], header=None, na_values=["NIL"],
#                                na_filter=True, names=["cellname", "time"], index_col='time')
#         df_cells_list.append(df_cells["cellname"].tolist())
#
#     if df_cells_list:
#         cells = np.unique(df_cells_list)

# for x in cells:
#     learningpercell(x)

learningpercell("83875d5954ea388720d8cc1a49dda122")