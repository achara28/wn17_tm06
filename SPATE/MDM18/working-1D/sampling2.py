import gzip
from subprocess import check_call
import numpy as np
import os.path
import os
import pandas as pd
import sys

filename = "sampling2_results.txt"
results = open(filename, 'a')

# print(sys.argv[1])
working_dir = "C:/Users/Andreas/Desktop/nms/"
# working_dir = "C:/Users/Andreas/Desktop/nms/ystr=2016"
# working_dir = "C:/Users/Andreas/Desktop/nms/ystr=2016/ymstr=1/ymdstr=24"
df_list = []
for root, dirs, files in os.walk(working_dir):
    file_list = []

    for filename in files:
        if filename.endswith('.csv'):
            file_list.append(os.path.join(root, filename))
    for file in file_list:
        df = pd.read_csv(file, delimiter="|", usecols=[1, 2, 6, 7], header=None, na_values=["NIL"],
                         na_filter=True, names=["meas_info", "counter", "value", "time"], index_col='time')
        df = df[df["counter"] == int(sys.argv[1])]

        df_list.append(df[["value"]])

if df_list:
    rawdata = pd.concat(df_list)

results.write("Total memory: ")
results.write(str(rawdata.values.nbytes) + "\n")
results.write("Total records: ")
results.write(str(len(rawdata)) + "\n")
sampling = []
counter = 0
decay_ratio = 0.5
slicedDf = []

end = int(len(rawdata) * (decay_ratio))
rate = 1  # 1/2


print(end)
print(rate)

for x in range(0, end):

    if counter == rate:
        sampling.append(rawdata['value'].iloc[x - 1])
        counter = 0
    else:
        sampling.append(rawdata['value'].iloc[x])

    counter += 1
    slicedDf.append(rawdata['value'].iloc[x])

print("")

# print(end)
rmse = np.sqrt((np.asarray((np.subtract(sampling, slicedDf))) ** 2).mean())
results.write("RMSE : %s \n" % rmse)
results.flush()
results.close()
