package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Device node in a network.
 * Date: 20/12/2016
 * Time: 16:57
 */
@JsonIgnoreType
public class NodeInfo {

    private long id;
    private String name;
    private String deviceType;

    private NodeInfo parent;
    private List<NodeInfo> children = new ArrayList<>();

    public NodeInfo() {
    }

    public NodeInfo(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.deviceType = device.getDeviceType().getName();
    }

    @JsonIgnore
    public boolean isRoot(){
        return this.parent == null;
    }
    @JsonIgnore
    public boolean isLeaf(){
        return children.isEmpty();
    }
    @JsonIgnore
    public List<NodeInfo> getChildren(){
        return Collections.unmodifiableList(children);
    }

    public void addChild(NodeInfo info) {
        if (children.add(info)) {
            info.setParent(this);
        }
    }

    public LinkInfo asLinkInfo(){
        if (!isRoot()) {
            return new LinkInfo(parent.getId(), this.getId(), 0);
        }
        return null;
    }

    public NodeInfo getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isGateWay() {
        return this.getParent() == null;
    }

    public NodeInfo getParent() {
        return parent;
    }

    public void setParent(NodeInfo parent) {
        this.parent = parent;
    }
}
