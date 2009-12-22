/*
 * CosemObject.java
 *
 * Created on 20 augustus 2004, 13:36
 */

package com.energyict.dlms.cosem;

import java.io.IOException;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;

/**
 *
 * @author Koen
 */
public interface CosemObject {

	long getValue() throws IOException;

	Date getCaptureTime() throws IOException;

	ScalerUnit getScalerUnit() throws IOException;

	Quantity getQuantityValue() throws IOException;

	String toString();

	Date getBillingDate() throws IOException;

	int getResetCounter();

	String getText() throws IOException;

}
