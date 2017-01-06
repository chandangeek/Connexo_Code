/*
 * ObisCodeMapper.java
 *
 * Created on 21 December 2005, 14:22
 */

package com.elster.us.protocolimpl.landisgyr.quad4;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author fbo
 */

class ObisCodeMapper {

    /**
     * Collection for sorting the keys
     */
    private List<ObisCodeWrapper> keys = new ArrayList<>();
    /**
     * HashMap with the ValueFactories per ObisCode
     */
    private Map<ObisCodeWrapper, ValueFactory> oMap = new HashMap<>();

    int dataBlkTblSize[];
    HashMap dMap = new HashMap();

    Quad4 quad4;

    /**
     * Creates a new instance of ObisCodeMapping
     */
    ObisCodeMapper(Quad4 quad4) throws IOException {
        this.quad4 = quad4;
        init();
    }

    /**
     * @return a RegisterInfo for the obiscode
     */
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    /**
     * @return a RegisterValue for the obiscode
     */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        ValueFactory vFactory = get(obisCode);
        if (vFactory == null) {
            throw new NoSuchRegisterException();
        }
        return vFactory.getRegisterValue(obisCode);
    }

    /**
     * Retrieves objects from the ObisCodeMap
     */
    public ValueFactory get(ObisCode o) {
        return oMap.get(new ObisCodeWrapper(o));
    }

    /**
     * Add objects to the ObisCodeMap
     */
    public void put(ObisCode o, ValueFactory f) {
        ObisCodeWrapper ocw = new ObisCodeWrapper(o);
        keys.add(ocw);
        oMap.put(ocw, f);
    }

    Table14 getTable14() throws IOException {
        return quad4.getTable14();
    }

    Table8 getTable8() throws IOException {
        return quad4.getTable8();
    }

    /**
     * @return construct extended logging
     */
    public String getExtendedLogging() throws IOException {
        StringBuilder builder = new StringBuilder();
        List<ObisCode> obisList = getMeterSupportedObisCodes();
        for (ObisCode obc : obisList) {
            builder
                    .append(obc.toString())
                    .append(" ")
                    .append(getRegisterInfo(obc))
                    .append("\n");
        }
        return builder.toString();
    }

    /**
     * @return get Values for all available obiscodes
     */
    public String getDebugLogging() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (ObisCode o : getMeterSupportedObisCodes()) {
            ValueFactory vf = oMap.get(new ObisCodeWrapper(o));
            builder.append(o).append(" ").append(vf).append("\n");
            builder.append(getRegisterValue(o)).append("\n");
        }
        return builder.toString();
    }

    /**
     * @return short desciption of ALL the possibly available obiscodes
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("All possibly supported ObisCodes \n");
        for (ObisCodeWrapper key : keys) {
            ValueFactory vf = oMap.get(key);
            builder
                    .append(key)
                    .append(" ")
                    .append(vf.toString())
                    .append("\n");
        }
        return builder.toString();
    }

    /**
     * This is the init for the actual values, this method does not
     * read any register configuration information, since that requires
     * communication.
     *
     * @throws IOException
     */
    private void init() throws IOException {

        int maxSelfReads = quad4.getTable0().getTypeMaximumValues().getMaxSelfReads();

        /* current */
        init(255, 0);
        /* past season */
        init(128, getDataBlkTblSize());

        /* billing points */
        int offset = getDataBlkTblSize();
        for (int i = 0; i < maxSelfReads; i++) {
            offset += getDataBlkTblSize();
            init(i, offset);
        }

    }

    /* In order to calculate the offset, it is necessary to know the size
     * of a (one season/billingpoint) DataBlkTbl.
     */
    private int getDataBlkTblSize() throws IOException {
        if (dataBlkTblSize == null) {
            int dateSize = 6;
            TypeMaximumValues tmv = quad4.getTable0().getTypeMaximumValues();

            int maxDataBlks = tmv.getMaxDataBlks();
            int maxSummations = tmv.getMaxSummations();
            int maxConcValues = tmv.getMaxConcValues();

            int summationRcdSize = 8 + 2;
            int rateBlkSize = dateSize + (8 * maxConcValues);

            int dataBlkRcdSize = maxSummations * summationRcdSize;
            dataBlkRcdSize += maxConcValues * rateBlkSize;
            dataBlkRcdSize += maxConcValues * 2;
            dataBlkRcdSize += 8;

            dataBlkTblSize = new int[1];
            dataBlkTblSize[0] = dataBlkRcdSize * maxDataBlks;
            dataBlkTblSize[0] += dateSize;
            dataBlkTblSize[0] += 2;
        }
        return dataBlkTblSize[0];
    }

    private Date getBillingPointDate(int billingPoint) throws IOException {
        Date d = (Date) dMap.get(new Integer(billingPoint));
        if (d == null) {
            int offset = 0;
            if (billingPoint == 255) {
                offset = 0;
            }
            if (billingPoint == 128) {
                offset = getDataBlkTblSize();
            }
            if (billingPoint >= 0 && billingPoint <= quad4.getTable0().getTypeMaximumValues().getMaxSelfReads()) {
                offset = getDataBlkTblSize() * (billingPoint + 2);
            }
            d = new TableAddress(quad4, 15, offset).readDate();
        }
        return d;
    }

    private void init(final int billing, final int offset) throws IOException {
        ObisCode o = ObisCode.fromString("1.1.0.1.2." + billing);// create obiscodes for time register
        put(o, new ValueFactory(o) {
            Quantity getQuantity() {
                return null;
            }

            RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
                return new RegisterValue(obisCode, new TableAddress(quad4, 15, offset).readDate());
            }
        });

        final Table14 t14 = quad4.getTable14();

        Bus b = Bus.TOTALIZATION_BUS;
        LineSelect l = LineSelect.VARH;

        Set<Integer> dataBlocksMapped = new HashSet<>();
        Iterator si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            int dataBlockIndex = rcd.getDataBlockIndex();
            int rowIndex = rcd.getRowIndex();
            String obisPartE;
            if (!dataBlocksMapped.contains(dataBlockIndex)) {
                dataBlocksMapped.add(dataBlockIndex);
                obisPartE = Integer.toString(dataBlockIndex);
            } else {
                obisPartE = Integer.toString(dataBlockIndex) + Integer.toString(rowIndex);
            }
            o = ObisCode.fromString("1.1.5.9." + obisPartE + "" + billing);
            //System.out.println("mapping obis code " + o + " to " + rcd);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.PLUS_WH_DELIVERED;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.2.9." + rcd.getDataBlockIndex() + "" + billing);
            //System.out.println("mapping obis code " + o + " to " + rcd);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.PLUS_Q2_VARH_INDUCTIVE_LAGGING;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.6.9." + rcd.getDataBlockIndex() + "" + billing);

            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.PLUS_Q3_VARH_CAPACITIVE_LEADING;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.7.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.PLUS_Q4_VARH_CAPACITIVE_LEADING;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();

            o = ObisCode.fromString("1.1.8.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        ////////////////////

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.WH_RECEIVED_A;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.21.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.WH_DELIVERED_A;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.22.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q1_A;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.25.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q2_A;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.26.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q3_A;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.27.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q4_A;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.28.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }


        // B /////////////////

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.WH_RECEIVED_B;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.41.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.WH_DELIVERED_B;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.42.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q1_B;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.45.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q2_B;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.46.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q3_B;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.47.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q4_B;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.48.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        // C /////////////////

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.WH_RECEIVED_C;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.61.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.WH_DELIVERED_C;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.62.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q1_C;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.65.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q2_C;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.66.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q3_C;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.67.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

        b = Bus.MTR_INPUT_BUS;
        l = LineSelect.Q4_C;

        si = t14.getSummations(b, l).iterator();
        while (si.hasNext()) {
            final TypeChannelSelectRcd rcd = (TypeChannelSelectRcd) si.next();
            o = ObisCode.fromString("1.1.68.9." + rcd.getDataBlockIndex() + "" + billing);
            createValueFactory(offset, rcd, l, o);
        }

    }

    void createValueFactory(
            final int offset, final TypeChannelSelectRcd rcd, final LineSelect line,
            final ObisCode obis) {
        put(obis, new ValueFactory(obis) {
            Quantity getQuantity() throws IOException {
                Number n = (Number) rcd.getTableAddress(offset).read();

                //Unit u = getTable8().getInChnlCntrlRcd( line.getId() ).getUnit();
                Unit u = line.getUnit();
                //System.out.println("offset: " + offset + ", rcd: " + rcd + ", line: " + line + ", obis: " + obis + ", unit: " + u);
                return new Quantity(n, u);
            }
        });

    }

    /**
     * @return list of all ObisCodes supported by the currently connected
     * meter.  Does this by trial and error.
     */
    private List<ObisCode> getMeterSupportedObisCodes() throws IOException {
        List<ObisCode> validObisCodes = new ArrayList<>();
        for (ObisCodeWrapper key : keys) {
            ObisCode oc = key.obisCode;
            // if no exception is thrown, the ObisCode is supported
            try {
                getRegisterValue(oc);
                validObisCodes.add(oc);
            } catch (NoSuchRegisterException nre) {
                // if an exception is thrown, the ObisCode is not available.
                //nre.printStackTrace();
            }
        }
        return validObisCodes;
    }

    /**
     * Shorthand notation for throwing NoSuchRegisterException
     *
     * @throws NoSuchRegisterException
     */
    private void throwException(ObisCode obisCode) throws NoSuchRegisterException {
        String ob = obisCode != null ? obisCode.toString() : "unknown";
        String msg = "ObisCode " + ob + " is not supported!";
        throw new NoSuchRegisterException(msg);
    }

    /**
     * the java version of a closure ( aka a nice function pointer )
     */
    abstract class ValueFactory {
        ObisCode obisCode = null;

        ValueFactory(ObisCode o) {
            obisCode = o;
        }

        Quantity getQuantity() throws IOException {
            return null;
        }

        ;

        Date getFromTime() throws IOException {
            return null;
        }

        ;

        Date getToTime() throws IOException {
            return getBillingPointDate(obisCode.getF());
        }

        ;

        Date getEventTime() throws IOException {
            if (obisCode.getF() == 255) {
                return getBillingPointDate(obisCode.getF());
            } else {
                return null;
            }
        }

        ;

        ObisCode getObisCode() throws IOException {
            return obisCode;
        }

        ;

        RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
            Quantity q = getQuantity();
            if (q == null) {
                throwException(obisCode);
            }
            Date e = getEventTime();
            Date f = getFromTime();
            Date t = getToTime();
            return new RegisterValue(obisCode, q, e, f, t);
        }

        public String toString() {
            return obisCode.toString();
        }

    }

    /**
     * The ObisCodeMapper works with a Map that links the available obis
     * codes to ValueFactories that can retrieve data from the RegisterFactory.
     * <p>
     * The keys of the Map are actuall ObisCodes.  But the equal method of
     * obis codes makes a distinction between relative period (VZ) and
     * absolute periods.  This is not the behaviour that is needed here.
     * ObisCodeWrapper will provide the ObisCodes with an equals and hash
     * method that does not make a distinction between relative and absolute
     * periods.
     */
    static class ObisCodeWrapper implements Comparable {

        private ObisCode obisCode;

        private String os;
        private String reversedOs;

        ObisCodeWrapper(ObisCode oc) {
            obisCode = oc;

            os = obisCode.getA() + "" + obisCode.getB() + "" +
                    obisCode.getC() + "." + obisCode.getD() + "." +
                    obisCode.getE() + "." + Math.abs(obisCode.getF());

            reversedOs = new StringBuffer(os).reverse().toString();
        }

        public boolean equals(Object o) {
            if (!(o instanceof ObisCodeWrapper)) {
                return false;
            }

            ObisCodeWrapper other = (ObisCodeWrapper) o;
            return os.equals(other.os);
        }

        public int hashCode() {
            return os.hashCode();
        }

        public String toString() {
            return "ObisCode: " + obisCode;
        }

        public int compareTo(Object o) {
            ObisCodeWrapper other = (ObisCodeWrapper) o;
            return reversedOs.compareTo(other.reversedOs);
        }

    }

}