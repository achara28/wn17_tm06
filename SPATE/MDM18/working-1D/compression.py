import gzip
from subprocess import check_call
import numpy as np
import os.path
import os
import pandas as pd
import sys

filename = "compression_results.txt"
results = open(filename, 'a')

working_dir = "C:/Users/Andreas/Desktop/nms/"
#working_dir = "C:/Users/Andreas/Desktop/nms/ystr=2016/ymstr=1/ymdstr=24"
df_list = []
for root, dirs, files in os.walk(working_dir):
    file_list = []

    for filename in files:
        if filename.endswith('.csv'):
            file_list.append(os.path.join(root, filename))
    for file in file_list:
        df = pd.read_csv(file, delimiter="|", usecols=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10], header=None,
                         na_values=["NIL"],
                         na_filter=True, names=["col0", "meas_info", "counter", "col3", "col4", "col5", "value",
                                                "time", "col8", "col9", "col10"])
        df = df[df["counter"] == int(sys.argv[1])]
        # df = df[df["counter"] == 50331671]
        #        print(df[["value"]])
        # df_list.append(df[["value"]])
        df_list.append(df)

        # print(file)
if df_list:
    rawdata = pd.concat(df_list)
    #print(rawdata)

results.write("Total size of %s\n" % rawdata.values.nbytes)

# print(df)

# select sampling rate


results.write("Total memory: ")
results.write(str(rawdata.values.nbytes)+" \n")
results.write("Total records: ")
results.write(str(len(rawdata)) + "\n")
rate = 2  # 1/2
sampling = []
counter = 0
decay_ratio = 0.5
slicedDf = []

end = int(len(rawdata) * decay_ratio)
outfilename = "slicedDf.txt.gz"
output = gzip.open(outfilename, 'w')

for x in range(0, end):
    # data = bytes(str(rawdata['time'].iloc[x]) " "+ str(rawdata['meas_info'].iloc[x])+ " " + str(rawdata['counter'].iloc[x])+" "+str(rawdata['value'].iloc[x]) + "\n", 'utf-8')

    data = bytes(str(rawdata['time'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['meas_info'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['counter'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['value'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col3'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col4'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col5'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col8'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col9'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col10'].iloc[x]) + "|", 'utf-8')
    output.write(data)
    data = bytes(str(rawdata['col0'].iloc[x]) + "\n", 'utf-8')
    output.write(data)

output.close()

results.write(str(outfilename)+ ' contains '+ str(os.stat(outfilename).st_size)+ ' bytes of compressed data \n')
