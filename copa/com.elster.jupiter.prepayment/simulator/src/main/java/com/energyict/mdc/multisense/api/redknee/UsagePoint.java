/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.security.SecureRandom;

/**
 * Created by bvn on 9/17/15.
 */
@XStreamAlias("UsagePoint")
public class UsagePoint {
    private SecureRandom random;
    private String mRID;
    private String device_mRID;
    private MinMax consumption;
    private Status status = Status.connected;

    public UsagePoint() {
    }

    public UsagePoint(String mRID, String device_mRID, MinMax consumption, Status status) {
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

    public MinMax getConsumption() {
        return consumption;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "UsagePoint{" +
                "consumption=from " + consumption.getMin() + " till " + consumption.getMax() +
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

class MinMax {
    private double min, max;

    public MinMax() {
    }

    public MinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}

enum Status {
    connected, disconnected, armed
}
