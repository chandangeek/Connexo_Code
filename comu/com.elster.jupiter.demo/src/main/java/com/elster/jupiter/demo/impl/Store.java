package com.elster.jupiter.demo.impl;

import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {
    private Map<String, Object> properties;
    private Map<String, RegisterType> registerTypes;
    private Map<String, RegisterGroup> registerGroups;
    private Map<String, LoadProfileType> loadProfileTypes;
    private Map<String, LogBookType> logBookTypes;
    private Map<String, ComTask> comTasks;
    private Map<String, OutboundComPortPool> outboundComPortPools;
    private Map<String, ComSchedule> comSchedules;

    private Map<Class, List<?>> objetcs;

    public Store() {
        properties = new HashMap<>();
        registerTypes = new HashMap<>();
        registerGroups = new HashMap<>();
        loadProfileTypes = new HashMap<>();
        logBookTypes = new HashMap<>();
        comTasks = new HashMap<>();
        outboundComPortPools = new HashMap<>();
        comSchedules = new HashMap<>();

        objetcs = new HashMap<>();
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

    public Map<String, ComSchedule> getComSchedules() {
        return comSchedules;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public <T> List<T> get(Class<T> clazz){
        List<T> list = (List<T>) objetcs.get(clazz);
        return list != null ? list : Collections.<T>emptyList();
    }

    public <T> void add(Class<T> clazz, T obj){
        List<T> list = (List<T>) objetcs.get(clazz);
        if (list == null){
            list = new ArrayList<>();
            objetcs.put(clazz, list);
        }
        list.add(obj);
    }
}