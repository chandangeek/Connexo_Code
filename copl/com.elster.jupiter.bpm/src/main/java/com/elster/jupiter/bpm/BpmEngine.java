package com.elster.jupiter.bpm;

import java.util.List;

public interface BpmEngine {
    String getLocation();

    void setLocation(String location);

    String getName();

    void setName(String name);

    void save();

    List<String> getProcesses();

    long startProcessInstance(String process);
}
