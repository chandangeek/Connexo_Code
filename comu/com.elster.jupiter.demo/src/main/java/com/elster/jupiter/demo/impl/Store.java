package com.elster.jupiter.demo.impl;

import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.tasks.ComTask;

import java.util.HashMap;
import java.util.Map;

public class Store {
    private Map<String, RegisterType> registerTypes;
    private Map<String, RegisterGroup> registerGroups;
    private Map<String, LoadProfileType> loadProfileTypes;
    private Map<String, LogBookType> logBookTypes;
    private Map<String, ComTask> comTasks;
    private Map<String, OutboundComPortPool> outboundComPortPools;

    public Store() {
       registerTypes = new HashMap<String, RegisterType>();
       registerGroups = new HashMap<String, RegisterGroup>();
       loadProfileTypes = new HashMap<String, LoadProfileType>();
       logBookTypes = new HashMap<String, LogBookType>();
       comTasks = new HashMap<String, ComTask>();
        outboundComPortPools = new HashMap<String, OutboundComPortPool>();
    }

    public Map<String, RegisterType> getRegisterTypes() {
        return registerTypes;
    }

    public Map<String, RegisterGroup> getRegisterGroups() {
        return registerGroups;
    }

    public Map<String, LoadProfileType> getLoadProfileTypes() {
        return loadProfileTypes;
    }

    public Map<String, LogBookType> getLogBookTypes() {
        return logBookTypes;
    }

    public Map<String, ComTask> getComTasks() {
        return comTasks;
    }

    public Map<String, OutboundComPortPool> getOutboundComPortPools() {
        return outboundComPortPools;
    }
}
