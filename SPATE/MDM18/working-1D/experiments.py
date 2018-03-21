import os

counters = [67179779,
             67179778,
             67189840,
             67180565,
             67180566,
             73424223,
             73423494,
             67189400,
             67189401,
             73423510]


#counters=[67179779]




# #####################   SAMPLING 2   ##########################

# filename = "sampling2_results.txt"
# file = open(filename, 'a')
# file.write(
#     "------------------------------------\nExperiments for counters\n------------------------------------\n")
#
# for counter in counters:
#     file.write(
#         "\n------------------------------------\n")
#     file.write("Counter : %s\n" % counter)
#     file.write(
#         "------------------------------------\n")
#     file.flush()
#     os.system("sampling2.py %s" % counter)
#     file.write(
#         "------------------------------------\n")
#
# file.close()
# ####################################################
#
#
#
# #####################   SAMPLING   ##########################

# filename = "sampling_results.txt"
# file = open(filename, 'a')
# file.write(
#     "------------------------------------\nExperiments for counters\n------------------------------------\n")
#
# for counter in counters:
#     file.write(
#         "\n------------------------------------\n")
#     file.write("Counter : %s\n" % counter)
#     file.write(
#         "------------------------------------\n")
#     file.flush()
#     os.system("tst.py %s" % counter)
#     file.write(
#         "------------------------------------\n")
#
# file.close()
# ####################################################
#


#####################   PTP   ##########################

filename = "ptp_resultsRNNall.txt"
file = open(filename, 'a')
file.write(
    "------------------------------------\nExperiments for counters\n------------------------------------\n")

for counter in counters:
    file.write(
        "\n------------------------------------\n")
    file.write("Counter : %s\n" % counter)
    file.write(
        "------------------------------------\n")
    file.flush()
    os.system("telco_mdm.py %s" % counter)
    file.write(
        "------------------------------------\n")

file.close()
####################################################

#
# #####################   COMPRESSION   ##########################
# filename = "compression_results.txt"
# file = open(filename, 'a')
# file.write(
#     "------------------------------------\nExperiments for counters\n------------------------------------\n")
# #counters = [67190703, 67192702]
#
# for counter in counters:
#     file.write(
#         "\n------------------------------------\n")
#     file.write("Counter : %s\n" % counter)
#     file.write(
#         "------------------------------------\n")
#     file.flush()
#     os.system("compression.py %s" % counter)
#     file.write(
#         "------------------------------------\n")
#
# file.close()
####################################################