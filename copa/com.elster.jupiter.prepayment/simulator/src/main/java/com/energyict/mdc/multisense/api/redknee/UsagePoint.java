package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by bvn on 9/17/15.
 */
@XStreamAlias("UsagePoint")
public class UsagePoint {
    private String mRID;
    private String device_mRID;
    private String readingType;
    private double consumption;
    private Status status = Status.connected;

    public String getmRID() {
        return mRID;
    }

    public String getDevice_mRID() {
        return device_mRID;
    }

    public String getReadingType() {
        return readingType;
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
                "readingType='" + readingType + '\'' +
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
