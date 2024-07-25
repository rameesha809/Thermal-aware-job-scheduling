package org.cloudbus.cloudsim.examples;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.examples.TempeeratureModel;

import java.io.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

public class two {
    private static List<Cloudlet> cloudletList;
    private static List<Cloudlet> subList;
    private static List<Vm> vmList;
    private static TempeeratureModel tempeeratureModel;
    private static List<List<Double>> temperatureData = new ArrayList<>();
    private static boolean fileCleared = false;
    private static boolean file2Cleared = false;
    private static Map<Integer, Integer> cloudletToVmMap = new HashMap<>();
    private static int cloudletId = 0;
    private static int now=0;
    private static int p=0;
    public static void main(String[] args) {
        Log.printLine("Starting CloudSimExample3...");
       

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);
            Datacenter datacenter = createDatacenter();
            System.out.println("Time zone: " + CloudSim.clock());
            System.out.println("Cloudlet default length: " + CloudSim.getMinTimeBetweenEvents());
                   
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();
         // Access the list of hosts from the datacenter
            List<Host> hostList = datacenter.getHostList();

            vmList = createVms(brokerId, datacenter,1023);

            broker.submitVmList(vmList);
            tempeeratureModel = new TempeeratureModel(vmList);
         
            cloudletList = createCloudletsFromCSV(brokerId, "\job-data2c.csv");
//            System.out.println("Cloudlet default input size: " + CloudSim.getNumberOfCloudlets());

            broker.submitCloudletList(cloudletList);
            //calculateAndAddTemperatureChange(cloudletList, tempeeratureModel);
            int finishedCloudlets = 0;
            CloudSim.startSimulation();
            List<Double> temperatureChange = new ArrayList<>();
            while (finishedCloudlets < cloudletList.size()) {
             //   CloudSim.pauseSimulation();
                int batchSize = 500; // Set your desired batch size
                int toIndex = Math.min(finishedCloudlets + batchSize, cloudletList.size());
                calculateAndAddTemperatureChange(cloudletList.subList(finishedCloudlets, toIndex), tempeeratureModel);               
                finishedCloudlets += batchSize;
              //  CloudSim.resumeSimulation();
              //  if (finishedCloudlets < cloudletList.size()) {
                	//currentColumn.add(Integer.toString(temperatureData.size()));

                   // System.out.println("Temperature after " + finishedCloudlets + " cloudlets:");
                  
                    for (Vm vm : vmList) {
                    	int serverId = vm.getId();
                    	//System.out.println(serverId);
                    	double temperature = tempeeratureModel.getTemperature(serverId);
                    	
                    	temperatureChange.add(temperature);
                    	//currentColumn.add(temperatureEntry);
                   
                    }
                   
                    //temperatureData.clear();
                    temperatureData.add(temperatureChange);
              //      System.out.println(temperatureData);
                 //   saveTemperatureDataToCSV("temperature_data.csv",temperatureData);
                    temperatureChange.clear();
                    temperatureData.clear();
                   }
             //   }
                   
            
            CloudSim.stopSimulation();
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);
            CloudSim.stopSimulation();
            cloudletList = null;

         
         // Display final temperatures of all servers
           /* for (Host host : hostList) {
            	int serverId = host.getId();
            	double temperature = tempeeratureModel.getTemperature(serverId);
            	System.out.println("Server " + serverId + " Temperature: " + temperature);
            }*/


            Log.printLine("CloudSimExample3 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }
   
    private static void saveTemperatureDataToCSV(String fileName, List<List<Double>> temperatureData) {
        try (FileWriter writer = new FileWriter(fileName,true)) {
        	
            // Write the server IDs in the first row
        	
        	if (temperatureData.isEmpty()) {
                System.out.println("No temperature data to save.");
                return;
            }
        	//writer.write("Server IDs,");
        	
             // If it's the first write, clear the file and write the header
        	if (!fileCleared == true) {
                writer.write("Cloudlet ID,");
                for (int i = 1; i <= 1023; i++) {
                    writer.write("ser" + i + ",");
                }
                //writer.write("\n");
                fileCleared = true; // Set the flag to indicate that the file has been cleared
            }
            writer.write("\n");
          // int i=0;
           for (cloudletId = 0; cloudletId < temperatureData.size(); cloudletId++) {
                writer.write(cloudletId + ","); // Write the cloudlet ID

                List<Double> serverTemperatures = temperatureData.get(cloudletId);
                for (Double temperature : serverTemperatures) {
                    writer.write(temperature + ","); // Write the server temperatures
                }
               // loop++;

                //writer.write("\n");
           }
               // loop++;

            System.out.println("Temperature data has been saved to " + fileName);
           // temperatureData.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private static Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < 1023; i++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 1000;
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
            int hostId = i;
            int ram = 2048;
            long storage = 1000000;
            int bw = 10000;
            hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter("Datacenter", characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    
    private static void calculateAndAddTemperatureChange(List<Cloudlet> cloudlets, TempeeratureModel tempeeratureModel) {
    	try (FileWriter writer = new FileWriter("temperaturesforDNN.csv", true)) {
            // If it's the first write, clear the file and write the header
       	if (!file2Cleared == true) {
               writer.write("Cloudlet ID,");
               writer.write("CPU Time,");
               writer.write("CPUs Used,");
               writer.write("Start Time,");
               writer.write("Finish Time,");
            //   writer.write("Server Assigned,");
               for (int i = 1; i <= 1023; i++) {
                   writer.write("ser" + i + ",");
               }
               //writer.write("\n");
               file2Cleared = true; // Set the flag to indicate that the file has been cleared
               writer.write("\n");
           }
         
    	for (Cloudlet cloudlet : cloudlets) {
    		int serverId = cloudlet.getVmId(); // Use VM ID as the server ID
        	double executionTime = cloudlet.getCloudletLength();
        	double temperatureChange =0;
        	double temperatureAdd =0;
        	if(executionTime>= 1 && executionTime<=10000) {
        		temperatureChange = 1;
        		temperatureAdd =0;
        	}
        	else if(executionTime>= 10001 && executionTime<=20000){
        		temperatureChange = 3;
        		temperatureAdd =1;
        	}
        	else if(executionTime>= 20001 && executionTime<=50000){
        		temperatureChange = 4;
        		temperatureAdd =2;
        	}
        	else if(executionTime>= 50001 && executionTime<=75000){
        		temperatureChange = 5;
        		temperatureAdd =2;
        	}
        	else if(executionTime>= 75001 && executionTime<=90000){
        		temperatureChange = 6;
        		temperatureAdd =2;
        	}
        	else {
        		temperatureChange = 7;
        		temperatureAdd =3;
        	}
            //double temperatureChange = executionTime *0.003; // Adjust the coefficient as needed.
        	int next= 0;
        	int morenext=0;
            int prev=0;
            int moreprev=0;
            // Update the temperature in the TempeeratureModel
            tempeeratureModel.updateTemperature(serverId, temperatureChange);
            if(serverId!=1022) {
            next= serverId+1;
            tempeeratureModel.updateTemperature(next, temperatureAdd);
            }
            if(serverId!=0) {
            prev=serverId-1;
            tempeeratureModel.updateTemperature(prev, temperatureAdd);
            }
            if(serverId<34) {
            	morenext=serverId+33;
            	tempeeratureModel.updateTemperature(morenext, temperatureAdd);
            }
            if(serverId>=34&&serverId<=991) {
            	moreprev=serverId-33;
            	morenext=serverId+33;
            	tempeeratureModel.updateTemperature(moreprev, temperatureAdd);
            	tempeeratureModel.updateTemperature(morenext, temperatureAdd);
            }
            if(serverId>991) {
            	moreprev=serverId-33;
            	tempeeratureModel.updateTemperature(moreprev, temperatureAdd);
            }
        
             	writer.write(cloudlet.getCloudletId() + ",");
                writer.write(cloudlet.getCloudletLength() + ",");
                writer.write( cloudlet.getNumberOfPes() + ",");           // Access cpusUsed
                writer.write( cloudlet.getExecStartTime() + ",");
                writer.write( cloudlet.getFinishTime() + ",");
            //    writer.write( serverId + ","); 
                for (Vm vm : vmList) {
                	 int sId = vm.getId();
                	//System.out.println(serverId);
                	double temperature = tempeeratureModel.getTemperature(sId);
                	writer.write(temperature + ",");
                	//currentColumn.add(temperatureEntry);
               
                }
                writer.write("\n");
               // loop++;
    	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Vm> createVms(int brokerId,Datacenter datacenter, int vmCount) {
        List<Vm> vms = new ArrayList<>();

        for (int i = 0; i < 1023; i++) {
            int vmid = i;
            int mips = 1000;
            long size = 10000;
            int ram = 512;
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";
            Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vm.setHost(datacenter.getHostList().get(i));
            vms.add(vm);
        }

        return vms;
    }

    private static List<Cloudlet> createCloudletsFromCSV(int brokerId, String csvFile) {
        List<Cloudlet> cloudlets = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line;
            //int cloudletId = 0;
            boolean isHeaderLine = true; // Flag to identify the header line

            while ((line = br.readLine()) != null) {
            	if (isHeaderLine) {
            	       // Skip the header line
            	       isHeaderLine = false;
            	       continue;
            	}
                String[] values = line.split(",");
                if (values.length < 3) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }
                
                int jobId;
                int finishTime, startTime, cpuTime;
                int cpusUsed;
                
                try {
                    jobId = Integer.parseInt(values[0]);
                    startTime = Integer.parseInt(values[3]);
                    finishTime = Integer.parseInt(values[4]);
                    cpuTime = Integer.parseInt(values[6]);
                    cpusUsed = Integer.parseInt(values[7]);
                    
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing values in line: " + line);
                    continue;
                }

                UtilizationModel utilizationModel = new UtilizationModelFull();
                Cloudlet cloudlet = new Cloudlet(jobId, (long) cpuTime, cpusUsed, 0, 0, utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudlet.setVmId(getVmIdForCloudlet(now));
                now++;

                cloudlets.add(cloudlet);

                //jobId++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cloudlets;
    }
     
    private static int getVmIdForCloudlet(int cloudletId) {
        // This is a simple round-robin mapping of cloudlets to VMs
        return  now % vmList.size();
    }



    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
        DecimalFormat dft = new DecimalFormat("###.##");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }
}
