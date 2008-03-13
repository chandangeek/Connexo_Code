/*
 * CosemObject.java
 *
 * Created on 20 augustus 2004, 13:36
 */

package com.energyict.dlms.cosem;

import java.util.*;
import java.io.*;
import com.energyict.protocolimpl.dlms.*;
import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;
/**
 *
 * @author  Koen
 */
public interface CosemObject {
    public long getValue() throws IOException;
    public Date getCaptureTime() throws IOException;
    public ScalerUnit getScalerUnit() throws IOException;
    public Quantity getQuantityValue() throws IOException;
    public String toString();
    public Date getBillingDate() throws IOException;
    public int getResetCounter();
    public String getText() throws IOException;
}
