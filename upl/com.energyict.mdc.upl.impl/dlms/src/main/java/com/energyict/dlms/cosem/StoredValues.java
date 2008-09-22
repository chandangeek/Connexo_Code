/*
 * StoredValues.java
 *
 * Created on 13 oktober 2004, 15:29
 */

package com.energyict.dlms.cosem;

import java.io.*;
import java.util.*;

import com.energyict.obis.ObisCode;
/**
 *
 * @author  Koen
 */
public interface StoredValues {
    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException;
    public Date getBillingPointTimeDate(int billingPoint) throws IOException;
    public int getBillingPointCounter() throws IOException;
    public void retrieve() throws IOException;
    public ProfileGeneric getProfileGeneric();
}
