package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class MeterFilter {
    private String mrid;
    private List<String> states = new ArrayList<>();

    public String getMrid() {
        return mrid;
    }

    public void setMrid(String name) {
        this.mrid = name;
    }

    public List<String> getStates(){
        return states;
    }

    public void addState(String status){
        states.add(status);
    }
}
