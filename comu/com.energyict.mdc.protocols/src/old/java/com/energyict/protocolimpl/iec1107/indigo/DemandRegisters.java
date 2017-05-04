/*
 * DemandRegisters.java
 *
 * Created on 7 juli 2004, 12:38
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class DemandRegisters extends AbstractLogicalAddress {

    // KV TO_DO read demand definition to assign the right unit to the MD & CMD register
    final Unit[] risingDemandUnits={Unit.get("kW"),Unit.get("kW"),Unit.get("kVA"),Unit.get("kvar")};
    static final int[] OBISCMAPPINGRISINGDEMAND={1,2,9,129}; // apparent power configurable and reactive power is unknown in the doc, so, manufacturer specific...

    static public final int NR_OF_REGISTERS=12;
    static public final int NR_OF_RISING_DEMANDS=4;
    static public final int NR_OF_MAXIMUM_DEMANDS=4;
    static public final int NR_OF_CUMULATIVE_MAXIMUM_DEMANDS=4;
    String meanings[]={"ActiveImportRising","ActiveExportRising","ApparentRising","ReactiveRising","MaximumDemand1","MaximumDemand2","MaximumDemand3","MaximumDemand4","CumulativeMaximumDemand1","CumulativeMaximumDemand2","CumulativeMaximumDemand3","CumulativeMaximumDemand4"};

    Quantity[] risingValues = new Quantity[NR_OF_RISING_DEMANDS];
    Quantity[] maximumDemandValues = new Quantity[NR_OF_MAXIMUM_DEMANDS];
    Quantity[] cumulativeMaximumDemandValues = new Quantity[NR_OF_CUMULATIVE_MAXIMUM_DEMANDS];
    Date[] dates = new Date[NR_OF_MAXIMUM_DEMANDS];
    /** Creates a new instance of DemandRegisters */
    public DemandRegisters(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandRegisters: ");
        for(int i=0;i<NR_OF_RISING_DEMANDS;i++) {
            if (i>0) strBuff.append(", ");
            strBuff.append(meanings[i]+"="+risingValues[i].toString());
        }
        for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++) {
            strBuff.append(", ");
            strBuff.append(meanings[i+NR_OF_RISING_DEMANDS]+"="+maximumDemandValues[i].toString()+" (at "+dates[i]+")");
        }
        for(int i=0;i<NR_OF_CUMULATIVE_MAXIMUM_DEMANDS;i++) {
            strBuff.append(", ");
            strBuff.append(meanings[i+NR_OF_RISING_DEMANDS+NR_OF_MAXIMUM_DEMANDS]+"="+cumulativeMaximumDemandValues[i].toString());
        }
        return strBuff.toString();
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        for(int i=0;i<NR_OF_RISING_DEMANDS;i++)
            // KV TO_DO protocoldoc states that the value is little endian... analysis of protocol is not so...
            risingValues[i] = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,i*4,4)).movePointLeft(getScaler()),risingDemandUnits[i]);
        for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++)
            // KV TO_DO protocoldoc states that the value is little endian... analysis of protocol is not so...
            maximumDemandValues[i] = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,4*NR_OF_RISING_DEMANDS+i*4,4)).movePointLeft(getScaler()),getLogicalAddressFactory().getHistoricalData(getId()%0x100).getDemandUnit(i));
        for(int i=0;i<NR_OF_CUMULATIVE_MAXIMUM_DEMANDS;i++)
            // KV TO_DO protocoldoc states that the value is little endian... analysis of protocol is not so...
            cumulativeMaximumDemandValues[i] = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,4*NR_OF_RISING_DEMANDS+4*NR_OF_MAXIMUM_DEMANDS+i*4,4)).movePointLeft(getScaler()),getLogicalAddressFactory().getHistoricalData(getId()%0x100).getDemandUnit(i));

        // following the protocoldoc, the date is 4 bytes long and describes a julian date, nr of seconds since 00:00 1/1/1970
        // info about local/DST settings is elswhere in the protocol...
        for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++) {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTimeInMillis(ProtocolUtils.getLong(data,NR_OF_REGISTERS*4+i*4,4)*1000);
            dates[i] = calendar.getTime();
        }
    }

    public Quantity getDemandValue(String meaning) throws java.io.IOException {
        for(int i=0;i<NR_OF_REGISTERS;i++) {
            if (meanings[i].compareTo(meaning)==0) {
                if (i<NR_OF_RISING_DEMANDS) {
                    return risingValues[i];
                }
                else if (i<(NR_OF_MAXIMUM_DEMANDS+NR_OF_RISING_DEMANDS)) {
                    return maximumDemandValues[i-NR_OF_RISING_DEMANDS];
                }
                if (i<(NR_OF_MAXIMUM_DEMANDS+NR_OF_RISING_DEMANDS+NR_OF_CUMULATIVE_MAXIMUM_DEMANDS)) {
                    return maximumDemandValues[i-(NR_OF_RISING_DEMANDS+NR_OF_MAXIMUM_DEMANDS)];
                }
            }
        }
        throw new IOException("DemandRegisters, register "+meaning+" does not exist!");
    }

    public Quantity getRisingDemandValue(int i) {
        return risingValues[i];
    }
    public Quantity getMaximumDemandValue(int i) {
        return maximumDemandValues[i];
    }
    public Quantity getCumulativeMaximumDemandValue(int i) {
        return cumulativeMaximumDemandValues[i];
    }

    public Quantity getDemandValueforObisCAndD(int obisCodeC, int obisCodeD) throws java.io.IOException {
        if (obisCodeD == ObisCode.CODE_D_RISING_DEMAND) {
            for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++) {
                if (OBISCMAPPINGRISINGDEMAND[i]==obisCodeC) {
                    return risingValues[i];
                }
            }
        }
        else if (obisCodeD == ObisCode.CODE_D_MAXIMUM_DEMAND) {
            for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++) {
                int code = getDemandUnit(i);
                // KV TO_DO following the logical addresses specifications document,
                // 'Demand x demand units' can NEVER have a value <0 or >3
                // My experience is that is DOES happen!
                if ((code <= 3) && (HistoricalData.OBIS_C_MAPPING_MD_CMD[code]==obisCodeC)) {
                    return maximumDemandValues[i];
                }
            }
        }
        else if (obisCodeD == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) {
            for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++) {
                int code = getDemandUnit(i);
                // KV TO_DO following the logical addresses specifications document,
                // 'Demand x demand units' can NEVER have a value <0 or >3
                // My experience is that is DOES happen!
                if ((code <= 3) && (HistoricalData.OBIS_C_MAPPING_MD_CMD[code]==obisCodeC)) {
                    return cumulativeMaximumDemandValues[i];
                }
            }
        }

        throw new NoSuchRegisterException("DemandRegisters, register wit obis code C.D field "+obisCodeC+"."+obisCodeD+" does not exist!");
    }

    public Date getDemandTimestampforObisCAndD(int obisCodeC, int obisCodeD) throws java.io.IOException {
        if (obisCodeD == ObisCode.CODE_D_MAXIMUM_DEMAND) {
            for(int i=0;i<NR_OF_MAXIMUM_DEMANDS;i++) {
                int code = getDemandUnit(i);
                // KV TO_DO following the logical addresses specifications document,
                // 'Demand x demand units' can NEVER have a value <0 or >3
                // My experience is that is DOES happen!
                if ((code <= 3) && (HistoricalData.OBIS_C_MAPPING_MD_CMD[code]==obisCodeC)) {
                    return dates[i];
                }
            }
        }
        throw new NoSuchRegisterException("DemandRegisters, register wit obis code C.D field "+obisCodeC+"."+obisCodeD+" does not exist!");
    }

    private int getDemandUnit(int i) throws java.io.IOException {
       return getLogicalAddressFactory().getHistoricalData(getId()%0x100).getDemandUnits()[i];
    }

    public Date getDate(int mdIndex) {
        return dates[mdIndex];
    }


}
