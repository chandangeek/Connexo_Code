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

	/**
	 * @return
	 * @throws IOException
	 */
	long getValue() throws IOException;

	/**
	 * @return
	 * @throws IOException
	 */
	Date getCaptureTime() throws IOException;

	/**
	 * @return
	 * @throws IOException
	 */
	ScalerUnit getScalerUnit() throws IOException;

	/**
	 * @return
	 * @throws IOException
	 */
	Quantity getQuantityValue() throws IOException;

	String toString();

	/**
	 * @return
	 * @throws IOException
	 */
	Date getBillingDate() throws IOException;

	/**
	 * @return
	 */
	int getResetCounter();

	/**
	 * @return
	 * @throws IOException
	 */
	String getText() throws IOException;

}
