package com.energyict.mdc.device.topology.rest.info;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * Represents the link (path) between two nodes in a network.
 * Date: 20/12/2016
 * Time: 16:57
 */
@JsonIgnoreType
public class LinkInfo {

    private long source;
    private long target;
    private int linkQuality;

    public LinkInfo(){}

    LinkInfo(long source, long target, int linkQuality){
       this.source = source;
       this.target = target;
       this.linkQuality = linkQuality;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public int getLinkQuality() {
        return linkQuality;
    }

    public void setLinkQuality(int linkQuality) {
        this.linkQuality = linkQuality;
    }
}
