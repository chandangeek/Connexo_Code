package com.elster.jupiter.validation;

/**
 * Created by Lucian on 7/2/2015.
 */
public class ValidationSummary {
    Long id;
    String mrID;
    long suspects;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMrID() {
        return mrID;
    }

    public void setMrID(String mrID) {
        this.mrID = mrID;
    }


    public long getSuspects() {
        return suspects;
    }

    public void setSuspects(long suspects) {
        this.suspects = suspects;
    }

}