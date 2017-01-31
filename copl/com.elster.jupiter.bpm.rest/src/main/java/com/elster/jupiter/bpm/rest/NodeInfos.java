/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NodeInfos {
    public int total;

    public List<NodeInfo> nodes = new ArrayList<>();

    public NodeInfos() {
    }

    public NodeInfos(JSONArray nodes) {
        addAll(nodes);
    }

    void addAll(JSONArray nodeList) {
        if (nodeList != null) {
            List<NodeInfo> linkNodes = new ArrayList<>();
            for(int i = 0; i < nodeList.length(); i++) {
                try {
                    JSONObject node = nodeList.getJSONObject(i);
                    NodeInfo result = new NodeInfo(node);
                    if (result.type.equals("0")) {
                        nodes.add(result);
                        total++;
                    } else {
                        linkNodes.add(result);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            adjustNodes(nodes, linkNodes);
        }
    }

    private void adjustNodes(List<NodeInfo> nodes, List<NodeInfo> linkNodes) {
        for (NodeInfo node : nodes) {
            for (NodeInfo linkNode : linkNodes) {
                if (node.nodeId.equals(linkNode.nodeId)) {
                    node.state = node.COMPLETED;
                    break;
                }
            }
        }
    }
}
