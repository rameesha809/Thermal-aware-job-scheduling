package org.cloudbus.cloudsim.examples;

public class TemperatureChangeData {
    private int cloudletId;
    private int serverId;
    //private double temperatureChange;

    public TemperatureChangeData(int cloudletId, int serverId) {
        this.cloudletId = cloudletId;
        this.serverId = serverId;
        //this.temperatureChange = temperatureChange;
    }

    public int getCloudletId() {
        return cloudletId;
    }

    public int getServerId() {
        return serverId;
    }

   /* public double getTemperatureChange() {
        return temperatureChange;
    }*/
}
