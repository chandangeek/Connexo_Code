package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by bvn on 9/17/15.
 */
@XStreamAlias("UsagePoint")
public class UsagePoint {
    private String mRID;
    private String device_mRID;
    private double consumption;
    private Status status = Status.connected;

    public UsagePoint() {
    }

    public UsagePoint(String mRID, String device_mRID, double consumption, Status status) {
        this.mRID = mRID;
        this.device_mRID = device_mRID;
        this.consumption = consumption;
        this.status = status;
    }

    public String getmRID() {
        return mRID;
    }

    public String getDevice_mRID() {
        return device_mRID;
    }

    public double getConsumption() {
        return consumption;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "UsagePoint{" +
                "consumption='" + consumption + '\'' +
                ", device_mRID='" + device_mRID + '\'' +
                ", mRID='" + mRID + '\'' +
                "}\n";
    }

    public void connect() {
        this.status = Status.connected;
    }

    public void disconnect() {
        this.status = Status.disconnected;
    }
}

enum Status {
    connected, disconnected, armed
}
