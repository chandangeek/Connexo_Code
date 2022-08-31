package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Measure_Unit;

public class SimpleTelegram {

    private long timeStamp;
    private double power;
    private Measure_Unit powerUnit;
    private double energy;
    private Measure_Unit energyUnit;

    public SimpleTelegram() {
        timeStamp = new java.util.Date().getTime();
    }

    public SimpleTelegram(long _timeStamp) {
        timeStamp = _timeStamp;
    }

    public SimpleTelegram(double power, Measure_Unit powerUnit, double energy,
                          Measure_Unit energyUnit) {
        super();
        timeStamp = new java.util.Date().getTime();
        this.power = power;
        this.powerUnit = powerUnit;
        this.energy = energy;
        this.energyUnit = energyUnit;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public double getPower() {
        return power;
    }
    public void setPower(double power) {
        this.power = power;
    }
    public Measure_Unit getPowerUnit() {
        return powerUnit;
    }
    public void setPowerUnit(Measure_Unit powerUnit) {
        this.powerUnit = powerUnit;
    }
    public double getEnergy() {
        return energy;
    }
    public void setEnergy(double energy) {
        this.energy = energy;
    }
    public Measure_Unit getEnergyUnit() {
        return energyUnit;
    }
    public void setEnergyUnit(Measure_Unit energyUnit) {
        this.energyUnit = energyUnit;
    }

    public void debugOutput() {
        System.out.println("-----------SimpleTelegram----------");
        System.out.println("Power: " + this.power + " " + this.powerUnit);
        System.out.println("Energy: " + this.energy + " " + this.energyUnit);
    }
}