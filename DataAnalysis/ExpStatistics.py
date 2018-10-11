import csv;
import os;
import numpy as np;
from numpy import *

flatten = lambda list_of_lists: [item for sublist in list_of_lists for item in sublist]


directory = "/Users/keithburghardt/Dropbox/MultiplexExperiment/Experiment Data Files/";
os.chdir(directory);
read_file = "ParsedVariableExperimentData.csv";
write_file = "ParsedVariableExperimentData_Stats.csv";
NumNodes = 6;

r = open(read_file ,'r');
w = open(write_file ,'w');
csv_reader = csv.reader(r,delimiter=",");
csv_writer = csv.writer(w);
header = csv_reader.__next__();

exp_stats = [];
exp_stats_header = header[:14];

shocked = [];
profit = [];
fract_spillover = [];
clustering_coefficient = [];
degree = [];
num_ties = [];
mat1=[];
mat2=[];
demog = [];

line_num = -1;
for line in csv_reader:
    #print(line);
    line_num = line_num + 1;
    exp_stats.append(line[:14]);

    demog.append(line[28:]);
    # every 6th line
    if line_num % NumNodes == 0:
        if line_num > 0:
            #matrices
            mat1 = np.array(mat1);
            mat2 = np.array(mat2);
            # num ties
            d1d2 = np.array([np.sum(mat1,axis=0),np.sum(mat2,axis=0)]);
            d1d2 = np.ndarray.transpose(d1d2);
            d1d2 = np.ndarray.tolist(d1d2);
            ties = np.sum(d1d2,axis=1);
            #num_ties.append(ties);
            degree.append(d1d2);
            
            # fraction spillover
            mat1mat2 = np.multiply(mat1,mat2);
            spillover = np.sum(mat1mat2,axis=0);
            fs = np.multiply(2,np.divide(spillover,ties));
            fs = np.ndarray.tolist(fs);
            fract_spillover.append(fs);
            
            # clustering coefficient
            mat13 = np.matmul(mat1,np.matmul(mat1,mat1));
            num_tri1 = mat13.diagonal();
            d1 = np.sum(mat1,axis=0);
            clust1 = np.divide(num_tri1,np.multiply(d1,(d1-1)));
            where_are_NaNs = isnan(clust1);
            clust1[where_are_NaNs] = 0;
            mat23 = np.matmul(mat2,np.matmul(mat2,mat2));
            num_tri2 = mat23.diagonal();
            d2 = np.sum(mat2,axis=0);
            clust2 = np.divide(num_tri2,np.multiply(d2,(d2-1)));
            where_are_NaNs = isnan(clust2);
            clust2[where_are_NaNs] = 0;
            
            c1c2 = np.array([clust1,clust2]);
            c12 = np.ndarray.transpose(c1c2);
            c12 = np.ndarray.tolist(c12);

            clustering_coefficient.append(c12);
            
            mat1 = [];
            mat2 = [];
    mat1.append([float(v) for v in line[16:22]]);
    mat2.append([float(v) for v in line[22:28]]);
            
            
    shocked.append(line[14]);
    profit.append(line[15]);
# we now have an array of 
r.close();

header = exp_stats_header + ["shocked","profit","DegreeLayer1","DegreeLayer2","ClusteringCoefficientLayer1","ClusteringCoefficientLayer2","FractionSpillover","gender","year","major"]
    
#fract_spillover = np.ndarray.tolist(fract_spillover);
fract_spillover = flatten(fract_spillover);
#clustering_coefficient = np.ndarray.tolist(clustering_coefficient);
clustering_coefficient = flatten(clustering_coefficient);
#degree = np.ndarray.tolist(degree);
degree = flatten(degree);
    
    
write_mat = [flatten([stats,[s],[p],deg,clust,[spill],d]) for stats,s,p,deg,clust,spill,d in zip(exp_stats,shocked,profit,degree,clustering_coefficient,fract_spillover,demog)];
csv_writer.writerow(header);
for line in write_mat:
    csv_writer.writerow(line);
w.close();

