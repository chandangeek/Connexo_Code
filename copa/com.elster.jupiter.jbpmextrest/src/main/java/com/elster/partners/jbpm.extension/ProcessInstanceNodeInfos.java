package com.elster.partners.jbpm.extension;

import org.jbpm.kie.services.impl.model.VariableStateDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ProcessInstanceNodeInfos {

    public String processInstanceStatus;
    public String[] unwantedNodeType = new String[] {"AsyncEventNode", "BoundaryEventNode", "CatchLinkNode", "CompositeContextNode", "CompositeNode",
            "Constrainable", "ConstraintTrigger", "EventNode", "EventSubProcessNode", "EventTrigger", "FaultNode", "Join", "MilestoneNode", "Split",
            "StateBasedNode", "StateNode", "SubProcessNode", "ThrowLinkNode", "TimerNode", "Transformation", "Trigger"};
    public List<ProcessInstanceNodeInfo> processInstanceNodes = new ArrayList<ProcessInstanceNodeInfo>();
    public List<ProcessInstanceVariableInfo> processInstanceVariables = new ArrayList<ProcessInstanceVariableInfo>();

    public ProcessInstanceNodeInfos(){

    }

    public ProcessInstanceNodeInfos(List<Object[]> nodes, String processInstanceStatus, List<Object[]> processVariables){
        this.processInstanceStatus = processInstanceStatus;
        addAllNodes(nodes);
        addAllVariables(processVariables);
    }

    private void addAllNodes(List<Object[]> nodes){
        for(Object[] obj: nodes){
            addNode(obj);
        }
    }

    public ProcessInstanceNodeInfo addNode(Object[] object){
        ProcessInstanceNodeInfo processHistoryInfo = new ProcessInstanceNodeInfo(object, processInstanceStatus);
        if(!Arrays.asList(unwantedNodeType).contains(processHistoryInfo.nodeType)) {
            processInstanceNodes.add(processHistoryInfo);
        }
        return processHistoryInfo;
    }

    private ProcessInstanceVariableInfo addVariable(Object[] variable){
        ProcessInstanceVariableInfo processInstanceVariableInfo = new ProcessInstanceVariableInfo(variable, processInstanceNodes);
        processInstanceVariables.add(processInstanceVariableInfo);
        return processInstanceVariableInfo;
    }

    private void addAllVariables(List<Object[]> processVariables){
        for(Object[] variable : processVariables){
            addVariable(variable);
        }
    }

}
