package com.elster.jupiter.bpm;


import java.util.List;
import java.util.Map;

public interface ProcessAssociationProvider {

    String getName();

    List<Map<String,String>> getDataProperties(BpmProcessDefinition bpmProcessDefinition);

    String getType();

    void update(BpmProcessDefinition bpmProcessDefinition, List<Map<String,String>> associationData);
}
