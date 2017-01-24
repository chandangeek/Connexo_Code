/*
 * Counters.java
 *
 * Created on 24 maart 2004, 10:55
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.PactUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class Counters extends MeterReadingsBlockImpl {

	private Date meterDateTime;
	private int encryptedLoadSurveyAttempts;
	private int nonEncryptedLoadSurveyAttempts;
	private int mDResetsComm;
	private int mDResetsButton;
	private int hoursOnPower;
	private int hoursOffPower;
	private int readsOnPrioPort;
	private int readsOnNonPrioPort;
	private int successTariffDownLoads;
	private int failedPasswordClearAttempts;
	private int successTimeSet;
	private int billingActionsFromAllSources;
	private int hoursOnOverCurrent;
	private int hoursOnImbalance;
	private int minutesOnPowerSinceManuf;
	private int minutesOffPowerSinceManuf;
	private int cumulativeTransactions;
	private int cumulativeTransactionsAtLastBillingPoint;

	private int mask;

    /** Creates a new instance of Counters */
    public Counters(byte[] data, TimeZone timeZone) {
        super(data,timeZone);
    }

    protected void parse() throws IOException {
        int type = ProtocolUtils.byte2int(getData()[1]);

        if (type == 0) {
            setMeterDateTime(PactUtils.getCalendar2(ProtocolUtils.getIntLE(getData(),2,3),ProtocolUtils.getIntLE(getData(),5,3),getTimeZone()).getTime());
        }
        else if (type == 1) {
            setEncryptedLoadSurveyAttempts(ProtocolUtils.getIntLE(getData(),2,3));
            setNonEncryptedLoadSurveyAttempts(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 2) {
            setMDResetsComm(ProtocolUtils.getIntLE(getData(),2,3));
            setMDResetsButton(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 3) {
            setHoursOnPower(ProtocolUtils.getIntLE(getData(),2,3));
            setHoursOffPower(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 4) {
            setReadsOnPrioPort(ProtocolUtils.getIntLE(getData(),2,3));
            setReadsOnNonPrioPort(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 5) {
            setSuccessTariffDownLoads(ProtocolUtils.getIntLE(getData(),2,3));
            setFailedPasswordClearAttempts(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 6) {
            setSuccessTimeSet(ProtocolUtils.getIntLE(getData(),2,3));
        }
        else if (type == 7) {
            setBillingActionsFromAllSources(ProtocolUtils.getIntLE(getData(),2,3));
        }
        else if (type == 8) {
            setHoursOnOverCurrent(ProtocolUtils.getIntLE(getData(),2,3));
            setHoursOnImbalance(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 9) {
            // does not exist...
        }
        else if (type == 10) {
            setMinutesOnPowerSinceManuf(ProtocolUtils.getIntLE(getData(),2,3));
            setMinutesOffPowerSinceManuf(ProtocolUtils.getIntLE(getData(),5,3));
        }
        else if (type == 11) {
            setCumulativeTransactions(ProtocolUtils.getIntLE(getData(),2,3));
            setCumulativeTransactionsAtLastBillingPoint(ProtocolUtils.getIntLE(getData(),5,3));
        }
        mask |= (0x01 << type);
    } // parse()

    public int getBillingResetCounter() {
        return billingActionsFromAllSources+mDResetsButton+mDResetsComm;
    }

    protected String print() {
       StringBuffer strBuff = new StringBuffer();
       boolean pre = false;

       if ((mask & 0x0001) == 0x0001) {
           strBuff.append("Meter Date="+getMeterDateTime());
           pre = true;
       }
       if ((mask & 0x0002) == 0x0002) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Encrypted Load Survey read attempts="+getEncryptedLoadSurveyAttempts()+", Unencrypted Load Survey read attempts="+getNonEncryptedLoadSurveyAttempts());
           pre = true;
       }
       if ((mask & 0x0004) == 0x0004) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("MD resets triggered by communication="+getMDResetsComm()+", MD resets triggered by push button="+getMDResetsButton());
           pre = true;
       }
       if ((mask & 0x0008) == 0x0008) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Hours on power since 1/1/1988 00:00="+getHoursOnPower()+", Hours not on power since 1/1/1988 00:00="+getHoursOffPower());
           pre = true;
       }
       if ((mask & 0x0010) == 0x0010) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Reads on the priority (remote comm) port="+getReadsOnPrioPort()+", Reads on the non-priority (local comms) port="+getReadsOnNonPrioPort());
           pre = true;
       }
       if ((mask & 0x0020) == 0x0020) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Successful tariff downloads="+getSuccessTariffDownLoads()+", Failed password clearance attempts="+getFailedPasswordClearAttempts());
           pre = true;
       }
       if ((mask & 0x0040) == 0x0040) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Successful time setting attempts="+getSuccessTimeSet());
           pre = true;
       }
       if ((mask & 0x0080) == 0x0080) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Billing actions from all sources="+getBillingActionsFromAllSources());
           pre = true;
       }
       if ((mask & 0x0100) == 0x0100) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Hours on over-current="+getHoursOnOverCurrent()+", Hours on imbalance="+getHoursOnImbalance());
           pre = true;
       }
       if ((mask & 0x0400) == 0x0400) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Minutes on power since manufacture="+getMinutesOnPowerSinceManuf()+", Minutes off power since manufacture="+getMinutesOffPowerSinceManuf());
           pre = true;
       }
       if ((mask & 0x0800) == 0x0800) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("Cumulative transactions="+getCumulativeTransactions()+", Cumulative transactions at last billing point="+getCumulativeTransactionsAtLastBillingPoint());
           pre = true;
       }

       return strBuff.toString();
    }

    /** Getter for property meterDateTime.
     * @return Value of property meterDateTime.
     *
     */
    public java.util.Date getMeterDateTime() {
        return meterDateTime;
    }

    /** Setter for property meterDateTime.
     * @param meterDateTime New value of property meterDateTime.
     *
     */
    public void setMeterDateTime(java.util.Date meterDateTime) {
        this.meterDateTime = meterDateTime;
    }

    /** Getter for property encryptedLoadSurveyAttempts.
     * @return Value of property encryptedLoadSurveyAttempts.
     *
     */
    public int getEncryptedLoadSurveyAttempts() {
        return encryptedLoadSurveyAttempts;
    }

    /** Setter for property encryptedLoadSurveyAttempts.
     * @param encryptedLoadSurveyAttempts New value of property encryptedLoadSurveyAttempts.
     *
     */
    public void setEncryptedLoadSurveyAttempts(int encryptedLoadSurveyAttempts) {
        this.encryptedLoadSurveyAttempts = encryptedLoadSurveyAttempts;
    }

    /** Getter for property nonEncryptedLoadSurveyAttempts.
     * @return Value of property nonEncryptedLoadSurveyAttempts.
     *
     */
    public int getNonEncryptedLoadSurveyAttempts() {
        return nonEncryptedLoadSurveyAttempts;
    }

    /** Setter for property nonEncryptedLoadSurveyAttempts.
     * @param nonEncryptedLoadSurveyAttempts New value of property nonEncryptedLoadSurveyAttempts.
     *
     */
    public void setNonEncryptedLoadSurveyAttempts(int nonEncryptedLoadSurveyAttempts) {
        this.nonEncryptedLoadSurveyAttempts = nonEncryptedLoadSurveyAttempts;
    }

    /** Getter for property mDResetsComm.
     * @return Value of property mDResetsComm.
     *
     */
    public int getMDResetsComm() {
        return mDResetsComm;
    }

    /** Setter for property mDResetsComm.
     * @param mDResetsComm New value of property mDResetsComm.
     *
     */
    public void setMDResetsComm(int mDResetsComm) {
        this.mDResetsComm = mDResetsComm;
    }

    /** Getter for property mDResetsButton.
     * @return Value of property mDResetsButton.
     *
     */
    public int getMDResetsButton() {
        return mDResetsButton;
    }

    /** Setter for property mDResetsButton.
     * @param mDResetsButton New value of property mDResetsButton.
     *
     */
    public void setMDResetsButton(int mDResetsButton) {
        this.mDResetsButton = mDResetsButton;
    }

    /** Getter for property hoursOnPower.
     * @return Value of property hoursOnPower.
     *
     */
    public int getHoursOnPower() {
        return hoursOnPower;
    }

    /** Setter for property hoursOnPower.
     * @param hoursOnPower New value of property hoursOnPower.
     *
     */
    public void setHoursOnPower(int hoursOnPower) {
        this.hoursOnPower = hoursOnPower;
    }

    /** Getter for property hoursOffPower.
     * @return Value of property hoursOffPower.
     *
     */
    public int getHoursOffPower() {
        return hoursOffPower;
    }

    /** Setter for property hoursOffPower.
     * @param hoursOffPower New value of property hoursOffPower.
     *
     */
    public void setHoursOffPower(int hoursOffPower) {
        this.hoursOffPower = hoursOffPower;
    }

    /** Getter for property readsOnPrioPort.
     * @return Value of property readsOnPrioPort.
     *
     */
    public int getReadsOnPrioPort() {
        return readsOnPrioPort;
    }

    /** Setter for property readsOnPrioPort.
     * @param readsOnPrioPort New value of property readsOnPrioPort.
     *
     */
    public void setReadsOnPrioPort(int readsOnPrioPort) {
        this.readsOnPrioPort = readsOnPrioPort;
    }

    /** Getter for property readsOnNonPrioPort.
     * @return Value of property readsOnNonPrioPort.
     *
     */
    public int getReadsOnNonPrioPort() {
        return readsOnNonPrioPort;
    }

    /** Setter for property readsOnNonPrioPort.
     * @param readsOnNonPrioPort New value of property readsOnNonPrioPort.
     *
     */
    public void setReadsOnNonPrioPort(int readsOnNonPrioPort) {
        this.readsOnNonPrioPort = readsOnNonPrioPort;
    }

    /** Getter for property successTariffDownLoads.
     * @return Value of property successTariffDownLoads.
     *
     */
    public int getSuccessTariffDownLoads() {
        return successTariffDownLoads;
    }

    /** Setter for property successTariffDownLoads.
     * @param successTariffDownLoads New value of property successTariffDownLoads.
     *
     */
    public void setSuccessTariffDownLoads(int successTariffDownLoads) {
        this.successTariffDownLoads = successTariffDownLoads;
    }

    /** Getter for property failedPasswordClearAttempts.
     * @return Value of property failedPasswordClearAttempts.
     *
     */
    public int getFailedPasswordClearAttempts() {
        return failedPasswordClearAttempts;
    }

    /** Setter for property failedPasswordClearAttempts.
     * @param failedPasswordClearAttempts New value of property failedPasswordClearAttempts.
     *
     */
    public void setFailedPasswordClearAttempts(int failedPasswordClearAttempts) {
        this.failedPasswordClearAttempts = failedPasswordClearAttempts;
    }

    /** Getter for property successTimeSet.
     * @return Value of property successTimeSet.
     *
     */
    public int getSuccessTimeSet() {
        return successTimeSet;
    }

    /** Setter for property successTimeSet.
     * @param successTimeSet New value of property successTimeSet.
     *
     */
    public void setSuccessTimeSet(int successTimeSet) {
        this.successTimeSet = successTimeSet;
    }

    /** Getter for property billingActionsFromAllSources.
     * @return Value of property billingActionsFromAllSources.
     *
     */
    public int getBillingActionsFromAllSources() {
        return billingActionsFromAllSources;
    }

    /** Setter for property billingActionsFromAllSources.
     * @param billingActionsFromAllSources New value of property billingActionsFromAllSources.
     *
     */
    public void setBillingActionsFromAllSources(int billingActionsFromAllSources) {
        this.billingActionsFromAllSources = billingActionsFromAllSources;
    }

    /** Getter for property hoursOnOverCurrent.
     * @return Value of property hoursOnOverCurrent.
     *
     */
    public int getHoursOnOverCurrent() {
        return hoursOnOverCurrent;
    }

    /** Setter for property hoursOnOverCurrent.
     * @param hoursOnOverCurrent New value of property hoursOnOverCurrent.
     *
     */
    public void setHoursOnOverCurrent(int hoursOnOverCurrent) {
        this.hoursOnOverCurrent = hoursOnOverCurrent;
    }

    /** Getter for property hoursOnImbalance.
     * @return Value of property hoursOnImbalance.
     *
     */
    public int getHoursOnImbalance() {
        return hoursOnImbalance;
    }

    /** Setter for property hoursOnImbalance.
     * @param hoursOnImbalance New value of property hoursOnImbalance.
     *
     */
    public void setHoursOnImbalance(int hoursOnImbalance) {
        this.hoursOnImbalance = hoursOnImbalance;
    }

    /** Getter for property minutesOnPowerSinceManuf.
     * @return Value of property minutesOnPowerSinceManuf.
     *
     */
    public int getMinutesOnPowerSinceManuf() {
        return minutesOnPowerSinceManuf;
    }

    /** Setter for property minutesOnPowerSinceManuf.
     * @param minutesOnPowerSinceManuf New value of property minutesOnPowerSinceManuf.
     *
     */
    public void setMinutesOnPowerSinceManuf(int minutesOnPowerSinceManuf) {
        this.minutesOnPowerSinceManuf = minutesOnPowerSinceManuf;
    }

    /** Getter for property minutesOffPowerSinceManuf.
     * @return Value of property minutesOffPowerSinceManuf.
     *
     */
    public int getMinutesOffPowerSinceManuf() {
        return minutesOffPowerSinceManuf;
    }

    /** Setter for property minutesOffPowerSinceManuf.
     * @param minutesOffPowerSinceManuf New value of property minutesOffPowerSinceManuf.
     *
     */
    public void setMinutesOffPowerSinceManuf(int minutesOffPowerSinceManuf) {
        this.minutesOffPowerSinceManuf = minutesOffPowerSinceManuf;
    }

    /** Getter for property cumulativeTransactions.
     * @return Value of property cumulativeTransactions.
     *
     */
    public int getCumulativeTransactions() {
        return cumulativeTransactions;
    }

    /** Setter for property cumulativeTransactions.
     * @param cumulativeTransactions New value of property cumulativeTransactions.
     *
     */
    public void setCumulativeTransactions(int cumulativeTransactions) {
        this.cumulativeTransactions = cumulativeTransactions;
    }

    /** Getter for property cumulativeTransactionsAtLastBillingPoint.
     * @return Value of property cumulativeTransactionsAtLastBillingPoint.
     *
     */
    public int getCumulativeTransactionsAtLastBillingPoint() {
        return cumulativeTransactionsAtLastBillingPoint;
    }

    /** Setter for property cumulativeTransactionsAtLastBillingPoint.
     * @param cumulativeTransactionsAtLastBillingPoint New value of property cumulativeTransactionsAtLastBillingPoint.
     *
     */
    public void setCumulativeTransactionsAtLastBillingPoint(int cumulativeTransactionsAtLastBillingPoint) {
        this.cumulativeTransactionsAtLastBillingPoint = cumulativeTransactionsAtLastBillingPoint;
    }

}
