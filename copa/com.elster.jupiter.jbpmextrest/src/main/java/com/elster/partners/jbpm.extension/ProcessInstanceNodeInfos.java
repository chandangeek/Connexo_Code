/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.Arrays;
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
        processInstanceNodes.add(processHistoryInfo);
        return processHistoryInfo;
    }

    private ProcessInstanceVariableInfo addVariable(Object[] variable){
        List<ProcessInstanceNodeInfo> nodesWithVars = new ArrayList<ProcessInstanceNodeInfo>();
        for (ProcessInstanceNodeInfo node : processInstanceNodes) {
            if (!Arrays.asList(unwantedNodeType).contains(node.nodeType)) {
                nodesWithVars.add(node);
            }
        }

        ProcessInstanceVariableInfo processInstanceVariableInfo = new ProcessInstanceVariableInfo(variable, nodesWithVars);
        processInstanceVariables.add(processInstanceVariableInfo);
        return processInstanceVariableInfo;
    }

    private void addAllVariables(List<Object[]> processVariables){
        for(Object[] variable : processVariables){
            addVariable(variable);
        }
    }

}
