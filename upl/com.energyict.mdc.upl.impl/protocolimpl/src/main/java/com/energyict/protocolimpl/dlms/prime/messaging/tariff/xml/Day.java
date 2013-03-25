package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Day implements Serializable {

    protected List<Change> change;

    protected int id;

    public List<Change> getChange() {
        if (change == null) {
            change = new ArrayList<Change>();
        }
        return this.change;
    }

    /**
     * Gets the value of the id property.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     */
    public void setId(int value) {
        this.id = value;
    }

    public void addChange(Change change) {
        this.getChange().add(change);
    }

}
