/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm.register;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * <pre>
 * There are/should be 5 Billing periods:
 * - one current (billing count -1 )
 * - 4 historical ones (billing count 12-8)
 * 04 April 2005
 * There is a new and improved way of ordering the historicaldatasets.
 * The old way ordered the Hist. Datasets using dates.  But the timestamps on
 * the HistoricalData where only precise to the minute.  If a meteroperator
 * trembled during a billingpoint reset, 2 new billingpoints with exactly the
 * same date are created.  In that case the protocol can not know how to order
 * them based upon the dates.
 * Since the HistoricalDataSet used a TreeSet with Date as keys, the same
 * key is added twice.  So you loose 1 of the historicaldatasets, and it seems
 * as though there are only 3 billing points available.  In other words true
 * havoc is being caused.
 * The sollution is to sort on the billingpoint counter.  This is a 4-digit
 * counter with a range from 0000-9999.  The BillingPointComparator implements
 * this sorting.
 *
 *</pre>
 *@author fbo
 */

public class HistoricalDataSet {

	private Map billingPointMap = new TreeMap(new ReverseComparator(new BillingPointComparator()));
	private HistoricalData[] hd = null;

	public HistoricalDataSet() {
	}

	public void add(HistoricalData historicalData) {
		billingPointMap.put(new Integer(historicalData.getBillingCount()), historicalData);
		hd = (HistoricalData[]) billingPointMap.values().toArray(new HistoricalData[0]);
	}

	/* -1 = current, 0 = last billing point, 1 = 2-throws last billing point, ...*/
	public HistoricalData get(int billingCount) {
		return hd[billingCount];
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("\nHistorical Data Set (size=" + hd.length + ")\n\n");

		for (int i = 0; i < hd.length; i++) {
			sb.append("internal billing count = " + i + "\n");
			sb.append(hd[i].toString() + "\n");
		}

		return sb.toString();
	}

	/** The sorting has to take into account overflowing of the register. */
	class BillingPointComparator implements Comparator {

		/* @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
		public int compare(Object object1, Object object2) {
			Integer I1 = (Integer) object1;
			Integer I2 = (Integer) object2;

			int i1 = I1.intValue();
			int i2 = I2.intValue();

			if ((i1 > 9996) && (i2 < 9996)) {
				i2 = i2 + 10000;
			}

			if ((i2 > 9996) && (i1 < 9996)) {
				i1 = i1 + 10000;
			}

			if (i1 < i2) {
				return -1;
			}
			if (i1 > i2) {
				return 1;
			}
			return 0;
		}

	}

	class ReverseComparator implements Comparator {

		Comparator comparator = null;

		ReverseComparator(Comparator comparator) {
			this.comparator = comparator;
		}

		public int compare(Object object1, Object object2) {
			int r = comparator.compare(object1, object2);
			if (r > 0) {
				return -1;
			}
			if (r < 0) {
				return +1;
			}
			return 1;
		}

	}

}