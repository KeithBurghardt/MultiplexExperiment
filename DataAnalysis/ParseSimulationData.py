import csv;
import os;
import codecs;
import ast;
from contextlib import contextmanager;
import sys, os;
import time;

@contextmanager
def suppress_stdout():
    with open(os.devnull, "w") as devnull:
        old_stdout = sys.stdout
        sys.stdout = devnull
        try:  
            yield
        finally:
            sys.stdout = old_stdout

#from csv import csvreader


# read 16 x (2*NumNodes^2) lines
# Record attributes (link cost, triangle benefits, etc) from first line
# edge list are rightmost numbers in each line.
# Record player_i, player_j, edge(t=1), edge(t=2),...,edge(t=16)
# split files by N, combine with varying triangle benefits, spillover


directory="/Users/networklab/Documents/workspace/agents2/";
#simulation parameters

verbose = 0;
end_time = 12;


flatten = lambda list_of_lists: [item for sublist in list_of_lists for item in sublist]
roundlist = lambda list_to_round: [round(float(item),5) if item != '' else '' for item in list_to_round ]

#read_file_1 = ""

for read_file in [read_file_1]:
    
    #initialize to 0
    NumBatches=0;
    p1=read_file.find('NumSimulations=')+len('NumSimulations=');
    p2=read_file[p1:].find(',');
    NumSims=float(read_file[p1:p1+p2]);

    p1=read_file.find('N=')+len('N=');
    p2=read_file[p1:].find(',');
    NumNodes=float(read_file[p1:p1+p2]);


    if os.path.isfile(directory+read_file):
        print(read_file);
    else:
        print("ERROR: File not found: %s" %read_file);
        continue;
    r=open(directory+read_file,'r');

    read_csv_file=csv.reader(r, delimiter=',');
    
    #if read_file == read_file_1:
    header=read_csv_file.next();#__next__();
    print(header)
    header=["batch","run","Num_Players","noise","pre-tie cost","tri-benefit","spillover-payoff","one layer?","num_shocked","num_overlapped"]
    header.append("Shocked?");
    header.append("post-shock cost");
    header.append("layer");
    header.append("i");
    header.append("j");

    for t in range(end_time):
        header.append("t="+str(t+1)+"_edges");
        header.append("t="+str(t+1)+"_Recent Utility");
        header.append("t="+str(t+1)+"_Cumulative Utility");

    write_file=read_file[:-4]+"_Cleaned.csv";
    w = open(directory+write_file,"w");
    write_csv_file=csv.writer(w);
    write_csv_file.writerow(header);
    w.flush();
    os.fsync(w);
    #else: read_csv_file.next();

    ParamInfo = [];
    Network=[];
    AllTimeNetwork = [];
    time = 0;
    FinalShock = [];
 
    NumEdges=0;

    SimNumber = 0;

    NetworkWriteLine = [];
    deadspace_len = len(header)-(5+3*end_time);
    for i in range(deadspace_len):
        NetworkWriteLine.append("");
    #print(NetworkWriteLine);
    #
    count = 0;
    for row in read_csv_file:
        count = count + 1;

        #record data on first row:
        #if "NoSmartAgent" not in read_file_1:
        if NumEdges == 0 and time == 0:
            if verbose:
                print("Recording Network...");
            if SimNumber==0:
                NumBatches = NumBatches + 1;
                info=row[:];#read_csv_file.__next__();
                #print(len(info));
                if (len(info)<30):
                    print("ERROR");
                    print(info);
                    print(count);
                    break;
                # batch,run,Num_Players,noise,pre-tie cost, tri-benefit,spillover-payoff,one layer?,num_shocked,num_overlap
                #???,post-shock cost
                
                
                info = row[0:2]+[row[3]]+[row[4]]+row[8:10]+row[11:13]+row[15:17];
                #if read_file == read_file_2:
                #    prev_batch= float(info[0]);
                #    current_batch = prev_batch+100;
                info[0] = str(NumBatches);#str(current_batch);
            else: info[1]=str(SimNumber+1);
            # if no shock occurs
            if "Shock=None" in read_file:
                info[9] = info[4]; #pre-cost =  post-cost
            
        #record edges ONLY
        if NumEdges < 2*NumNodes**2  and time < end_time:

            if time == 0:
                
                Network.append(roundlist([row[-5]]+row[-7:-5]+row[-4:-1]));#+row[-9:-7]));

            else:
                #print(row)
                # indicates whether there is a connection from i-j
                if len(row)<9:
                    print(row);
                
                Network.append(roundlist(row[-4:-1]));
                if time == end_time-1:
                    if row[-7:-5] == [0,0]:
                        print("NewNetwork: 0,0\n");
                    #print(row[0])
                    FinalShock.append(row[0])
                
            NumEdges = NumEdges + 1;
            
        if NumEdges == 2*NumNodes**2 and time < end_time:
            if verbose:
                print("Network parsed for t="+str(time)+"...");

            AllTimeNetwork.append(Network);
            Network = [];
            time = time + 1;
            if time < end_time: NumEdges = 0;

        if NumEdges == 2*NumNodes**2 and time == end_time:

            # add cumulative utility
            
            for t in range(len(AllTimeNetwork)):
                AllTimeNetwork
            
            #write network to write_file
            write_line = [];
            write_edges = 0;
            if verbose:
                print("Writing Network to file...");
            #print("Writing Network to file...");
            line_num = 0;
            for write_edges in range(len(AllTimeNetwork[0])):
                
                write_line = [];
                edges_over_time = flatten([AllTimeNetwork[0][write_edges]] + [AllTimeNetwork[t][write_edges] for t in range(1,len(AllTimeNetwork))]);

                write_line = [];    
                if write_edges==0:# and SimNumber==0:
                    write_line=info[:];
                    
                else:
                    write_line = NetworkWriteLine[:];
                #if "NoSmartAgents" in read_file:
                write_line.append(FinalShock[write_edges]);
                
                for e in edges_over_time:
                    write_line.append(e);
                num_layers_shocked = float(info[7]);
                layer = int((line_num % (2*NumNodes**2))/(NumNodes**2));

                # node shocked?
                # in this section "if X==Y" is asking if preshock is equal to postshock (i.e., there is no shock)
                #print(len(write_line));
 
                if len(write_line)==19 or len(write_line)==20:
                        
                    if str(write_line[9]) == str(info[4]):#index = 10
                        write_line.insert(10,0)
                    else:
                        write_line.insert(10,1)
                        
                elif len(write_line)==50:
                    #(num_layers_shocked == 1 and layer == 1): if we are on layer "2" and only the first layer is shocked
                    if num_layers_shocked == 1 and layer == 1:
                        write_line[10] = info[4];
                        
                    if str(write_line[10]) == str(info[4]):#index = 10
                        write_line.insert(11,0)
                    else:
                        write_line.insert(11,1)
                elif len(write_line)==134:
                    #(num_layers_shocked == 1 and layer == 1): if we are on layer "2" and only the first layer is shocked
                    if num_layers_shocked == 1 and layer == 1:
                        write_line[10] = info[4];
                        
                    if str(write_line[10]) == str(info[4]):#index = 10
                        write_line.insert(11,0)
                    else: write_line.insert(11,1)
                else:
                    #(num_layers_shocked == 1 and layer == 1): if we are on layer "2" and only the first layer is shocked
                    if num_layers_shocked == 1 and layer == 1:
                        write_line[11] = info[4];
                        
                    if "UtilPerTurn=True" in read_file:
                        if str(write_line[11]) == str(info[4]):#index = 10
                            write_line[11]=0;
                        else: write_line[11]=1;
                    else:
                        if str(write_line[10]) == str(info[4]):#index = 10
                            write_line[11]=0;
                        else: write_line[11]=1;
                #print(write_line)
                
                write_csv_file.writerow([str(l) for l in write_line]);
                w.flush();
                os.fsync(w);
                line_num = line_num + 1;

            AllTimeNetwork = [];
            Network = [];
            time = 0;
            FinalShock = [];
            SimNumber = SimNumber+1;
            if verbose:
                print("Simulation number " + str(SimNumber) + "/" + str(NumSims));
            if SimNumber == NumSims:
                SimNumber = 0;
            NumEdges = 0;

    
    r.close();
    w.close();
