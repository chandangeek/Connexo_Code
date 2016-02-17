package com.elster.jupiter.bpm;


import java.util.List;
import java.util.Map;

public interface ProcessAssociationProvider {

    String getName();

    String getDisplayName();

    List<Map<String,String>> getAssociationData(BpmProcessDefinition bpmProcessDefinition);

    String getType();

    void update(BpmProcessDefinition bpmProcessDefinition, List<Map<String,String>> associationData);

    void checkPresentAssociationData(List associationData) throws IllegalStateException;
}
