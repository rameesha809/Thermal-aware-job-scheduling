package org.cloudbus.cloudsim.examples;
import java.util.Map;
import java.util.List;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.Host;
import java.util.HashMap;


public class TempeeratureModel {
	 private Map<Integer, Double> vmTemperatures;
	 private List<Vm> vmList; 
	 private List<Vm> sc; 

	    
	public TempeeratureModel(List<Vm> vmList) {
	    this.vmList = vmList;
	    vmTemperatures = new HashMap<>();
	}

    public void updateTemperature(int vmId, double change) {
        vmTemperatures.put(vmId, getTemperature(vmId) + change);
    }

    public double getTemperature(int vmId) {
        return vmTemperatures.getOrDefault(vmId, 70.0);
    }
    

}
