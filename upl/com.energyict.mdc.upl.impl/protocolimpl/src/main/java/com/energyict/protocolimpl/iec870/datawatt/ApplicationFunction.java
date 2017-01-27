/*
 * ApplicationFunction.java
 *
 * Created on 4 juli 2003, 16:22
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec870.CP56Time2a;
import com.energyict.protocolimpl.iec870.IEC870ASDU;
import com.energyict.protocolimpl.iec870.IEC870Connection;
import com.energyict.protocolimpl.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.iec870.IEC870InformationObject;
import com.energyict.protocolimpl.iec870.IEC870TransmissionCause;
import com.energyict.protocolimpl.iec870.IEC870TypeIdentification;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author  Koen
 * Changes:
 * KV 10062004 Add check on asdu type in getSpontASDU()
 */
public class ApplicationFunction {

    private static final int DEBUG=0;
    private static final int ORIGINATOR_ADDRESS=0;

    IEC870Connection iec870Connection=null;
    TimeZone timeZone=null;
    //Logger logger=null;

    List integratedTotals = null;
    List measuredNormValues = null;
    List singlePointInfos = null;

    /** Creates a new instance of ApplicationFunction */
    public ApplicationFunction(TimeZone timeZone,IEC870Connection iec870Connection,Logger logger) {
        //this.logger=logger;
        this.timeZone=timeZone;
        this.iec870Connection=iec870Connection;
    }

    public List getIntegratedTotals() {
       return integratedTotals;
    }
    public List getMeasuredNormValues() {
       return measuredNormValues;
    }
    public List getSinglePointInfos() {
       return singlePointInfos;
    }

    public void testASDU() throws IOException {
        try {
            byte[] objData = {(byte)0xAA,(byte)0x55};
            IEC870InformationObject io = new IEC870InformationObject(0);
            io.addData(objData);
            IEC870ASDU asdu = new IEC870ASDU(IEC870TypeIdentification.getId("C_TS_NA_1"), 1, IEC870TransmissionCause.getId("ACT"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io);
            iec870Connection.sendConfirm(asdu);
        }
        catch(IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, testProtocol, "+e.getMessage());
        }
    }

    public List historicalDataASDU(Calendar calendar) throws IOException {

        try {
            CP56Time2a cp56 = new CP56Time2a(calendar);
            IEC870InformationObject io = new IEC870InformationObject(0);
            io.addData(cp56.getData());
            io.addData(IEC870InformationObject.QOI_STATION_INTERROGATION);
            IEC870ASDU asdu = new IEC870ASDU(IEC870TypeIdentification.getId("C_IH_NA_P"), 1, IEC870TransmissionCause.getId("ACT"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io);
            List apdus = iec870Connection.sendConfirm(asdu);
            if (DEBUG >= 2) printAPDUList(apdus);
            parseASDUs(apdus);
            return (buildHistoricalValues());
        }
        catch(IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, historicalDataASDU, "+e.getMessage());
        }
    }

    private List buildHistoricalValues() {
       List historicalValues=new ArrayList();
       Iterator it;

       it = getIntegratedTotals().iterator();
       while(it.hasNext()) {
          IntegratedTotal itot = (IntegratedTotal)it.next();
          // KV_DEBUG KV 16072003 only include entries with timetag...
          //                      Possible?? --> spontaneous objects without timetag received during retrieving of historical data ???
          if (itot.isWithTimetag())
              historicalValues.add(new HistoricalValue(itot.getStatus(), itot.getValue(), itot.getDate(), itot.getChannel(), itot.isInvalid()));
          else System.out.println("KV_DEBUG>> buildHistoricalValues, IntegratedTotal without timestamp received, "+itot.toString());
       }

       it = getMeasuredNormValues().iterator();
       while(it.hasNext()) {
          MeasuredNormValue mnv = (MeasuredNormValue)it.next();
          // KV_DEBUG KV 16072003 only include entries with timetag...
          //                      Possible?? --> spontaneous objects without timetag received during retrieving of historical data ???
          if (mnv.isWithTimetag())
             historicalValues.add(new HistoricalValue(mnv.getStatus(), mnv.getValue(), mnv.getDate(), mnv.getChannel(), mnv.isInvalid()));
          else System.out.println("KV_DEBUG>> buildHistoricalValues, MeasuredNormValue without timestamp received, "+mnv.toString());
       }
       it = getSinglePointInfos().iterator();
       while(it.hasNext()) {
          SinglePointInfo spi = (SinglePointInfo)it.next();
          // KV_DEBUG KV 16072003 only include entries with timetag...
          //                      Possible?? --> spontaneous objects without timetag received during retrieving of historical data ???
          if (spi.isWithTimetag())
             historicalValues.add(new HistoricalValue(spi.getStatus(), spi.getValue(), spi.getDate(), spi.getChannel(), spi.isInvalid()));
          else System.out.println("KV_DEBUG>> buildHistoricalValues, SinglePointInfo without timestamp received, "+spi.toString());
       }

       Collections.sort(historicalValues);

       return historicalValues;
    }

    public void readASDU(int ioAddress) throws IOException {
        try {
            IEC870InformationObject io = new IEC870InformationObject(ioAddress);
            IEC870ASDU asdu = new IEC870ASDU(IEC870TypeIdentification.getId("C_RD_NA_1"), 1, IEC870TransmissionCause.getId("REQ"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io);
            List apdus = iec870Connection.sendConfirm(asdu);
            if (DEBUG >= 2) printAPDUList(apdus);
            parseASDUs(apdus);
        }
        catch(IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, historicalDataASDU, "+e.getMessage());
        }
    }

    public void clockSynchronizationASDU(Calendar calendar) throws IOException {
        try {
            CP56Time2a cp56 = new CP56Time2a(calendar);
            IEC870InformationObject io = new IEC870InformationObject(0);
            io.addData(cp56.getData());
            IEC870ASDU asdu = new IEC870ASDU(IEC870TypeIdentification.getId("C_CS_NA_1"), 1, IEC870TransmissionCause.getId("ACT"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io);
            List apdus = iec870Connection.sendConfirm(asdu);
            if (DEBUG >= 2) {
                printAPDUList(apdus);
            }
        } catch (IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, clockSynchronizationASDU, " + e.getMessage());
        }
    }

    public Calendar dsapGetClockASDU() throws IOException {
        try {
            byte[] objData = {(byte)0x34,(byte)0x34,(byte)0x1e,(byte)0x00,(byte)0x0a,(byte)0x01};
            IEC870InformationObject io = new IEC870InformationObject(0);
            io.addData(objData);
            List apdus = iec870Connection.sendConfirm(new IEC870ASDU(IEC870TypeIdentification.getId("X_DS_NA_P"), 0x80 | objData.length, IEC870TransmissionCause.getId("ACT"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io));
            if (DEBUG >= 2) printAPDUList(apdus);
            IEC870ASDU asdu=getSpontAPDU(apdus,IEC870TypeIdentification.getId("X_DS_NA_P"));
            Calendar calendar = Calendar.getInstance(timeZone);
 //System.out.println("KV_DEBUG> "+asdu.getTypeIdentification()+" "+asdu.getInformationObjects().size()+" "+asdu.getCauseOfTransmissionCause());
            if (asdu.getInformationObjects().size() < 13)
                throw new ProtocolException("DataWatt, dsapGetClockASDU, wrong nr of informationobjects, expected 13, received "+asdu.getInformationObjects().size());
            calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(asdu.getInformationObjectObjectData(7)[0])+2000);
            calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(asdu.getInformationObjectObjectData(8)[0])-1);
            calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(asdu.getInformationObjectObjectData(9)[0]));
            calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(asdu.getInformationObjectObjectData(10)[0]));
            calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(asdu.getInformationObjectObjectData(11)[0]));
            calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(asdu.getInformationObjectObjectData(12)[0]));
            return calendar;
        }
        catch(IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, dsapGetClockASDU, "+e.getMessage());
        }
    }

    public void interrogationCommandASDU() throws IOException {
        try {
            IEC870InformationObject io = new IEC870InformationObject(0);
            io.addData(0x14);
            IEC870ASDU asdu = new IEC870ASDU(IEC870TypeIdentification.getId("C_IC_NA_1"), 1, IEC870TransmissionCause.getId("ACT"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io);
            List apdus = iec870Connection.sendConfirm(asdu);
            if (DEBUG >= 2) printAPDUList(apdus);
            parseASDUs(apdus);
        }
        catch(IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, interrogationCommandASDU, "+e.getMessage());
        }
    }

    public void counterInterrogationCommandASDU() throws IOException {
        try {
            IEC870InformationObject io = new IEC870InformationObject(0);
            io.addData(0x05);
            IEC870ASDU asdu = new IEC870ASDU(IEC870TypeIdentification.getId("C_CI_NA_1"), 1, IEC870TransmissionCause.getId("ACT"), ORIGINATOR_ADDRESS, iec870Connection.getRTUAddress(), io);
            List apdus = iec870Connection.sendConfirm(asdu);
            if (DEBUG >= 2) printAPDUList(apdus);
            parseASDUs(apdus);
        }
        catch(IEC870ConnectionException e) {
            throw new ProtocolException("DataWatt, counterInterrogationCommandASDU, "+e.getMessage());
        }
    }

    /*
     * KV 10062004 Add check on asdu type because it seems that for some
     * Datawatt meters, also other asdu types are returned for the same cause.
     * that result in a wrong asdu retrieved from the asdu list!
     */
//    private IEC870ASDU getSpontAPDU(List list) throws IOException {
//        return getSpontAPDU(list,-1);
//    }

    private IEC870ASDU getSpontAPDU(List list,int typeIdentification) throws IOException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            IEC870ASDU asdu = (IEC870ASDU)it.next();
            if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("SPONT")) {
                if (typeIdentification == -1)
                    return asdu;
                else {
                    if (asdu.getTypeIdentification() == typeIdentification)
                        return asdu;
                }
            }
        }
        throw new IOException("DataWatt, getSpontAPDU, no spontaneous ASDU found!");
    }

    private void printAPDUList(List list) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            System.out.println(((IEC870ASDU)it.next()).toString(timeZone));
        }
    }

    private void parseASDUs(List asdus) throws IOException {
        integratedTotals = new ArrayList();
        measuredNormValues = new ArrayList();
        singlePointInfos = new ArrayList();

        Calendar calendar = null;
        Iterator it = asdus.iterator();
        while(it.hasNext()) {
            IEC870ASDU asdu = (IEC870ASDU)it.next();
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("C_IH_NA_P")) {
                if ((asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("SPONT")) ||
                    (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("ACTCON"))) {
                    CP56Time2a cp56 = new CP56Time2a(timeZone,asdu.getInformationObject().getObjData(),0);
                    calendar = cp56.getCalendar();
                }
            }
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("M_IT_TA_1")) {
                if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("INROGEN")) {
                    Iterator itio = asdu.getInformationObjects().iterator();
                    while(itio.hasNext()) {
                        IntegratedTotal integratedTotal = new IntegratedTotal((Calendar)calendar.clone(),timeZone,(IEC870InformationObject)itio.next());
                        integratedTotals.add(integratedTotal);
                    }
                }
            }
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("M_IT_NA_1")) {
                if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("REQCOGEN")) {
                    Iterator itio = asdu.getInformationObjects().iterator();
                    while(itio.hasNext()) {
                        IntegratedTotal integratedTotal = new IntegratedTotal(null,null,(IEC870InformationObject)itio.next());
                        integratedTotals.add(integratedTotal);
                    }
                }
            }
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("M_ME_TA_1")) {
                if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("INROGEN")) {
                    Iterator itio = asdu.getInformationObjects().iterator();
                    while(itio.hasNext()) {
                        MeasuredNormValue measuredNormValue = new MeasuredNormValue((Calendar)calendar.clone(),timeZone,(IEC870InformationObject)itio.next());
                        measuredNormValues.add(measuredNormValue);
                    }
                }
            }
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("M_ME_NA_1")) {
                if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("INROGEN")) {
                    Iterator itio = asdu.getInformationObjects().iterator();
                    while(itio.hasNext()) {
                        MeasuredNormValue measuredNormValue = new MeasuredNormValue(null,null,(IEC870InformationObject)itio.next());
                        measuredNormValues.add(measuredNormValue);
                    }
                }
            }
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("M_SP_TA_1")) {
                if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("INROGEN")) {
                    Iterator itio = asdu.getInformationObjects().iterator();
                    while(itio.hasNext()) {
                        SinglePointInfo singlePointInfo = new SinglePointInfo((Calendar)calendar.clone(),timeZone,(IEC870InformationObject)itio.next());
                        singlePointInfos.add(singlePointInfo);
                    }
                }
            }
            if (asdu.getTypeIdentification() == IEC870TypeIdentification.getId("M_SP_NA_1")) {
                if (asdu.getCauseOfTransmissionCause() == IEC870TransmissionCause.getId("INROGEN")) {
                    Iterator itio = asdu.getInformationObjects().iterator();
                    while(itio.hasNext()) {
                        SinglePointInfo singlePointInfo = new SinglePointInfo(null,null,(IEC870InformationObject)itio.next());
                        singlePointInfos.add(singlePointInfo);
                    }
                }
            }

        }

        if (DEBUG >= 1) printIntegratedTotalsList(integratedTotals);
        if (DEBUG >= 1) printMeasuredNormValuesList(measuredNormValues);
        if (DEBUG >= 1) printSinglePointInfosList(singlePointInfos);

    }


    private void printIntegratedTotalsList(List list) {
        if (list.size() > 0)
            System.out.println("****************************** printIntegratedTotalsList ******************************");
        Iterator it = list.iterator();
        while(it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }
    private void printMeasuredNormValuesList(List list) {
        if (list.size() > 0)
            System.out.println("****************************** printMeasuredNormValuesList ******************************");
        Iterator it = list.iterator();
        while(it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }
    private void printSinglePointInfosList(List list) {
        if (list.size() > 0)
            System.out.println("****************************** printSinglePointInfosList ******************************");
        Iterator it = list.iterator();
        while(it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

} // ApplicationFunction
