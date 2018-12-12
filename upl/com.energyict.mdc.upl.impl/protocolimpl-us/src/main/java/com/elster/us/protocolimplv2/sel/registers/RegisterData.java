package com.elster.us.protocolimplv2.sel.registers;

import java.util.Date;
import java.util.List;

public class RegisterData {
  private Date timestamp;
  private String description;
  private String unit;
  private String direction;
  private List<Bucket> buckets;
  
  
  public Date getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getUnit() {
    return unit;
  }
  public void setUnit(String unit) {
    this.unit = unit;
  }
  public String getDirection() {
    return direction;
  }
  public void setDirection(String direction) {
    this.direction = direction;
  }
  public List<Bucket> getBuckets() {
    return buckets;
  }
  public void setBuckets(List<Bucket> buckets) {
    this.buckets = buckets;
  }
  public Bucket getBucket(String id) {
    for(Bucket bucket : getBuckets()) {
      if(bucket.getId().equals(id)) {
        return bucket;
      }
    }
    return null;
  }

}
