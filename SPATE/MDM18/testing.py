from hdfs import InsecureClient
working_dir = "/user/canast02/"
hdfs = InsecureClient(url='http://pythia1.in.cs.ucy.ac.cy:50070', user='canast02')
fnames=hdfs.list(working_dir)

print(fnames)