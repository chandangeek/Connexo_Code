package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.rest.impl.DateConvertor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeInfo {

    @JsonIgnore
    static final int COMPLETED = 1;
    @JsonIgnore
    static final int IN_PROGRESS = 0;

    public String type;
    public String nodeType;
    public String nodeName;
    public String date;
    public int state;
    @JsonIgnore
    public String nodeId;
    @JsonIgnore
    public String connection;

    public NodeInfo() {
    }

    public NodeInfo(JSONObject node) {
        try {
            this.type = node.getString("type");
            this.nodeType = node.getString("nodeType");
            if (node.getString("nodeName").trim().length() > 0) {
                this.nodeName = node.getString("nodeName");
            } else {
                this.nodeName = "("+this.nodeType+")";
            }
            this.date = DateConvertor.convertTimeStamps(node.getString("date"), true);

            this.nodeId = node.getString("nodeId");
            this.connection = node.getString("connection");
            this.state = this.IN_PROGRESS;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
