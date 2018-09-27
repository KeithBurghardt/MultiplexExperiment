import os;
import csv;
import pandas as pd;

flatten = lambda list_of_lists: [item for sublist in list_of_lists for item in sublist]

#Spring 2017
SpringSessions = [
   "session_107_var_6_7_17_1030",
   "session_42_var_5_18_17_1630",
   "session_10_var_6_5_17_1430",
   "session_43_var_5_15_17_1300",
   "session_113_var_5_23_17_1430",
   "session_44_var_5_18_17_1630",
   "session_115_var_5_23_17_1430",
   "session_45_var_5_15_17_1300",
   "session_11_var_6_6_17_1430",
   "session_46_var_5_19_17_1020",
   "session_121_var_6_7_17_1030",
   "session_47_var_5_16_17_1400",
   "session_12_var_5_11_17_1230",
   "session_4_var_5_9_17_1530",
   "session_13_var_6_7_17_0920",
   "session_14_var_5_11_17_1230",
   "session_15_var_6_9_17_1000",
   "session_16_var_5_12_17_1100",
   "session_52_var_5_16_17_1230",
   "session_17_var_6_8_17_1710",
   "session_53_var_5_15_17_1100",
   "session_18_var_6_5_17_1430",
   "session_54_var_5_18_17_1520",
   "session_19_var_6_8_17_1710",
   "session_55_var_5_15_17_1100",
   "session_1_var_5_8_17_1550",
   "session_56_var_5_18_17_1520",
   "session_20_var_6_9_17_1130",
   "session_5_var_5_8_17_1700",
   "session_21_var_6_9_17_1000",
   "session_65_var_5_22_17_1100",
   "session_22_var_6_9_17_1130",
   "session_66_var_5_22_17_1300",
   "session_23_var_6_9_17_1300",
   "session_67_var_5_22_17_1100",
   "session_24_var_6_9_17_1500",
   "session_68_var_5_22_17_1300",
   "session_25_var_6_9_17_1300",
   "session_69_var_5_22_17_1530",
   "session_26_var_6_9_17_1500",
   "session_6_var_5_9_17_1630",
   "session_27_var_6_12_17_1430",
   "session_70_var_5_23_17_1030",
   "session_28_var_6_12_17_1540",
   "session_71_var_6_8_17_1600",
   "session_29_var_6_12_17_1430",
   "session_72_var_5_23_17_1030",
   "session_2_var_5_11_17_1120",
   "session_73_var_6_8_17_1600",
   "session_30_var_6_12_17_1540",
   "session_31_var_6_12_17_1650",
   "session_32_var_6_12_17_1650",
   "session_7_var_6_6_17_1230",
   "session_33_var_6_6_17_1100",
   "session_8_var_5_9_17_1630",
   "session_3_var_5_9_17_1400",
   "session_9_var_6_6_17_1430",
   "session_41_var_5_12_17_1320"];
#Summer I, 2017
SummerISessions = [
   "session_34_var_7_13_17_1300",
   "session_74_var_7_19_17_1300",
   "session_36_var_7_13_17_1430",
   "session_76_var_7_19_17_1300",
   "session_38_var_7_14_17_1000",
   "session_78_var_7_19_17_1430",
   "session_40_var_7_14_17_1300",
   "session_80_var_7_19_17_1430",
   "session_48_var_7_14_17_1300",
   "session_82_var_7_21_17_1000",
   "session_50_var_7_14_17_1430",
   "session_84_var_7_21_17_1130",
   "session_58_var_7_18_17_1000",
   "session_86_var_7_21_17_1130",
   "session_60_var_7_18_17_1130",
   "session_88_var_7_21_17_1300",
   "session_62_var_7_18_17_1300",
   "session_90_var_7_21_17_1300",
   "session_64_var_7_19_17_1000"];
#Summer II, 2017
SummerIISessions = [
    "session_100_var_8_25_17_1130",
   "session_94_var_8_24_17_1300",
   "session_102_var_8_25_17_1430",
   "session_96_var_8_24_17_1430",
   "session_92_var_8_24_17_1000",
   "session_98_var_8_24_17_1600",
   "session_104_var_9_6_17_1300",
   "session_106_var_9_6_17_1300",
   "session_108_var_9_6_17_1430",
   "session_110_var_9_6_17_1600",
   "session_112_var_9_8_17_1130",
   "session_114_var_9_8_17_1130"
   ];
#Fall, 2017
FallSessions = [
   "session_74var_11_3_2017_1230",
   "session_76var_11_3_2017_1230",
   "session_60var_11_2_2017_1400",
   "session_78var_11_3_2017_1400",
   "session_62var_11_3_2017_1100",
   "session_80var_11_3_2017_1400",
   "session_64var_11_3_2017_1100"
   ];
#Essex Data
EssexSessionList = [
   "session_10e_18_10_2017_1330",
   "session_11e_18_10_2017_1330", 
   "session_12e_18_10_2017_1530",
   "session_13e_18_10_2017_1530", 
   "session_14e_18_10_2017_1530",
   "session_15e_18_10_2017_1530", 
   "session_16e_19_10_2017_0930",
   "session_17e_19_10_2017_0930", 
   "session_18e_19_10_2017_1130",
   "session_19e_19_10_2017_1130", 
   "session_1e_18_10_2017_0930",
   "session_20e_19_10_2017_1130", 
   "session_21e-b_19_17_2017_1530",
   "session_22e_19_10_2017_1330", 
   "session_23e_19_10_2017_1330",
   "session_24e_19_10_2017_1330", 
   "session_25e_17_10_2017_1400",
   "session_26e_20_10_2017_1330", 
   "session_27e_17_10_2017_1400",
   "session_28e_19_10_2017_1330", 
   "session_29e_20_10_2017_1130",
   "session_2e-b_18_10_2017_1130", 
   "session_30e-b_20_10_2017_0930",
   "session_31e_17_10_2017_1400", 
   "session_32e_20_10_2017_1330",
   "session_33e_20_10_2017_1400", 
   "session_34e_19_10_2017_1530",
   "session_35e_20_10_2017_1130", 
   "session_36e_20_10_2017_0930",
   "session_37e_20_10_2017_1330", 
   "session_38e_20_10_2017_1330",
   "session_39e_20_10_2017_0930", 
   "session_3e_18_10_2017_0930",
   "session_40e_20_10_2017_0930", 
   "session_41e_20_10_2017_1130",
   "session_42e_20_10_2017_1130", 
   "session_43e_19_10_2017_1530",
   "session_44e_25_10_2017_1300", 
   "session_45e_20_10_2017_1530",
   "session_46e_20_10_2017_1530", 
   "session_47e_20_10_2017_1530",
   "session_48e-b_25_10_2017_1300", 
   "session_4e_18_10_2017_0930",
   "session_5e_18_10_2017_1130", 
   "session_6e_18_10_2017_1130",
   "session_7e_18_10_2017_1130", 
   "session_8e_18_10_2017_1330",
   "session_9e_18_10_2017_1330"
   ];

EndowmentSessions = ["session_endowpilot1_7_13_2018_1300", 
   "session_endowpilot2_7_13_2018_1300", 
   "session_endowpilot3_7_27_2018_1200"];
PrimingSessions = ["session_primepilot1_7_13_2018_1130", 
   "session_primepilot2_7_27_2018_1200"];
NumRounds = 12;

# For priming sessions: SessionList=PrimingSessions
# For endowment sessions: SessionList=EndowmentSessions
# For rest of experiments:
SessionList=SpringSessions+SummerISessions+SummerIISessions+FallSessions+EssexSessionList
ExpNumber = 0;
PlotData = [];
ParamList = [];
PlotDataIndiv = [];
SubjectDataSession = [];
PlotDataIndivShockedVsNot = [];
LinksAddedDroppedData = [];

write_header = ["ExperimentNumber","NumberOfNodes","TrainingPeriods","PreShockRounds","TieBenefit","TriangleBenefit","SpilloverBenefit","PreShockCost","PostShockCost","NumberNodesShocked","PointsMultiplier","Period","ActivePlayer","PlayerName","Shocked","Profit"];
for l in range(1,3):
    for i in range(1,7):
        write_header = write_header + ["PlayerConnectedToLayer-"+str(l)+"_Node"+str(i)];
            
directory = "/Users/keithburghardt/Dropbox/MultiplexExperiment/Experiment Data Files/";
write_file = "ParsedVariableExperimentData.csv";
w = open(directory+write_file,"w");
write_csv_file=csv.writer(w);
write_csv_file.writerow(write_header);
w.flush();
os.fsync(w);

for session in SessionList:
    directory = "/Users/keithburghardt/Dropbox/MultiplexExperiment/Experiment Data Files/";
    ExpNumber = ExpNumber + 1;
    if session in SpringSessions:
        directory = directory + "Revised Spring 2017/" + session + "/";
    elif session in SummerISessions:
        directory = directory + "Summer I 2017/" + session + "/";
    elif session in SummerIISessions:
        directory = directory + "Summer II 2017/" + session + "/";
    elif session in FallSessions:
        directory = directory + "Fall 2017/" + session + "/";
    elif session in EssexSessionList:
        directory = directory + "Essex October 2017/" + session + "/";
    elif session in EndowmentSessions or session in PrimingSessions:
        directory = directory + "Summer I 2018/" + session + "/";

    file = directory + session + ".csv";
    if os.path.isfile(file):
        print("reading: "+session+"\n");
    else:
        print("ERROR: File not found: " + file+"\n");
        continue;
    
    df = pd.read_csv(file);
    df.to_csv(file[:-4]+"_new.csv", line_terminator='\r\n', index=False)    
    r = open(file[:-4]+"_new.csv","r");
    csv_reader = csv.reader(r,delimiter=",");
    #header=csv_reader.next();

    globals_read = False;
    for row in csv_reader:
        row = [str(val) for val in row];
        if row[2] == "globals" and not row[4].isnumeric() and not globals_read:
            globals_variable_list = row[:];
        elif row[2] == "globals" and row[4].isnumeric()  and not globals_read:
            globals_read = True;
            NumNodesPos = globals_variable_list.index("NumNodes")
            NumNodes = round(float(row[NumNodesPos]));
            
            TrainingPeriodsPos = globals_variable_list.index("TrainingPeriods")
            TrainingPeriods=round(float(row[TrainingPeriodsPos]));
            
            PreShockRoundsPos = globals_variable_list.index("preShockRounds")
            PreShockRounds=round(float(row[PreShockRoundsPos]));
            if PreShockRounds > 12:
                PreShockRounds = 12;
            
            PointsMultiplierPos = globals_variable_list.index("PointsMultiplier")
            PointsMultiplier = float(row[PointsMultiplierPos])
            
            TieBenPos = globals_variable_list.index("benefit_of_next_tie")
            TieBen = float(row[TieBenPos])
            
            TriBenPos = globals_variable_list.index("benefit_of_closed_triangle")
            TriBen = float(row[TriBenPos])
            
            SpilloverBenPos = globals_variable_list.index("benefit_of_spillover")
            SpilloverBen = float(row[SpilloverBenPos])
            
            PreShockCostPos = globals_variable_list.index("startCostAll")
            PreShockCost = float(row[PreShockCostPos])
            
            PostShockCostPos = globals_variable_list.index("shockCostShocked");
            PostShockCost = float(row[PostShockCostPos])
            if PreShockRounds == 12:
                PostShockCost = PreShockCost
                
            NumShockedPos = globals_variable_list.index("numShocked")
            NumShocked = round(float(row[NumShockedPos]));
            if PreShockCost == PostShockCost:
                NumShocked = 0;

            GlobalVars=[NumNodes,TrainingPeriods,PreShockRounds,TieBen,TriBen,SpilloverBen,PreShockCost,PostShockCost,NumShocked,PointsMultiplier]
            SubjectVars = [];
        if row[2] == "globals" and row[4].isnumeric():
            active_playerPos = globals_variable_list.index("active_player")
            ActivePlayer=round(float(row[active_playerPos]))
        if row[2] == "subjects" and not row[4].isnumeric():
            subject_variable_list = row;
            
        if row[2] == "subjects" and row[4].isnumeric():

            #period
            PeriodPos = subject_variable_list.index("Period")
            Period = round(float(row[PeriodPos]));

            #player name
            player_namePos = subject_variable_list.index("player_name")
            player_name = round(float(row[player_namePos]));

            # will be shocked
            ShockedPos = subject_variable_list.index("shocked")
            Shocked = round(float(row[ShockedPos]));
            if PreShockCost == PostShockCost:
                Shocked = 0;

            #Profit
            ProfitPos = subject_variable_list.index("DisplayProfit")
            
            Profit = float(row[ProfitPos])

            # at end of period, subject is connected to:
            WhoConnected = [];
            
            for layer in range(1,3):
                WhoConnected.append([]);
                for player in range(1,int(NumNodes)+1):
                    tieName = "fill_S"+str(player)+"_"+str(layer);
                    tiePos = subject_variable_list.index(tieName);
                    tie = round(float(row[tiePos]));
                    WhoConnected[layer-1].append(tie);
            
            if Period > NumNodes*TrainingPeriods:
                SubjectVars.append(flatten([[""]*(len(GlobalVars)+1),[Period-NumNodes*TrainingPeriods],[ActivePlayer],[player_name],[Shocked],[Profit],flatten(WhoConnected)]));
    SubjectVars[0][0] = ExpNumber;
    for i in range(len(GlobalVars)):
        SubjectVars[0][i+1] = GlobalVars[i];
    for line in SubjectVars:
        write_csv_file.writerow(line);
        w.flush();
        os.fsync(w);
