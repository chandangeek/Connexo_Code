/*
 * CosemObject.java
 *
 * Created on 20 augustus 2004, 13:36
 */

package com.energyict.dlms.cosem;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public interface CosemObject {

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	long getValue() throws IOException;

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	Date getCaptureTime() throws IOException;

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	ScalerUnit getScalerUnit() throws IOException;

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	Quantity getQuantityValue() throws IOException;

	String toString();

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	Date getBillingDate() throws IOException;

	/**
	 * @return
	 */
	int getResetCounter();

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	String getText() throws IOException;

}
