package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class MeterFilter {
    private String name;
    private List<String> states = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getStates(){
        return states;
    }

    public void addState(String status){
        states.add(status);
    }
}
