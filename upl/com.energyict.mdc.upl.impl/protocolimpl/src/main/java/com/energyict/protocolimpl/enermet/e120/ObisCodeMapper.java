package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObisCodeMapper {

    private final E120 e120;

    /**
     * Collection for sorting the keys
     */
    private Set<ObisCodeWrapper> keys = new LinkedHashSet<>();
    /**
     * HashMap with the ValueFactories per ObisCode
     */
    private Map<ObisCodeWrapper, ValueFactory> oMap = new HashMap<>();

    ObisCodeMapper(E120 e120) throws IOException {
        this.e120 = e120;
        init();
    }

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        ValueFactory vFactory = get(obisCode);
        if (vFactory == null) {
            throw new NoSuchRegisterException();
        }
        return vFactory.getRegisterValue(obisCode);
    }

    public ValueFactory get(ObisCode o) {
        return oMap.get(new ObisCodeWrapper(o));
    }

    public void put(String oc, ValueFactory f) {
        f.obisCode = ObisCode.fromString(oc);
        ObisCodeWrapper ocw = new ObisCodeWrapper(f.obisCode);
        keys.add(ocw);
        oMap.put(ocw, f);
    }

    public String getExtendedLogging() throws IOException {
        StringBuilder builder = new StringBuilder();
        List obisList = getMeterSupportedObisCodes();
        for (ObisCode anObisList : (Iterable<ObisCode>) obisList) {
            ObisCode obc = anObisList;
            builder.append(obc.toString()).append(" ").append(getRegisterInfo(obc)).append("\n");
            builder.append(getRegisterValue(obc).toString()).append("\n");
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
            try {
                builder.append(key).append(" ").append(getRegisterInfo(key.obisCode)).append("\n");
            } catch (IOException e) {
                builder.append(key).append(" exception for info ");
                e.printStackTrace();
            }
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
    private void init() {

        // current
        put("1.1.1.8.0.255", new ValueFactory(15, 1, "Active import") {
            Date getToTime() {
                return null;
            }

            ;
        });
        // previous
        for (int i = 0; i < 255; i++) {
            put("1.1.1.8.0." + i, new ValueFactory(15, i + 1, "Active import"));
        }

        // current
        put("1.1.200.8.0.255", new ValueFactory(11, 1, "Tarif register 11") {
            Date getToTime() {
                return null;
            }

            ;
        });
        // previous
        for (int i = 0; i < 255; i++) {
            put("1.1.200.8.0." + i, new ValueFactory(11, i + 1, "Tarif register 11"));
        }

        // current
        put("1.1.201.8.0.255", new ValueFactory(12, 1, "Tarif register 12") {
            Date getToTime() {
                return null;
            }

            ;
        });
        // previous
        for (int i = 0; i < 255; i++) {
            put("1.1.201.8.0." + i, new ValueFactory(12, i + 1, "Tarif register 12"));
        }

        // current
        put("1.1.202.8.0.255", new ValueFactory(13, 1, "Tarif register 13") {
            Date getToTime() {
                return null;
            }

            ;
        });
        // previous
        for (int i = 0; i < 255; i++) {
            put("1.1.202.8.0." + i, new ValueFactory(13, i + 1, "Tarif register 13"));
        }

        // current
        put("1.1.203.8.0.255", new ValueFactory(14, 1, "Tarif register 14") {
            Date getToTime() {
                return null;
            }

            ;
        });
        // previous
        for (int i = 0; i < 255; i++) {
            put("1.1.203.8.0." + i, new ValueFactory(14, i + 1, "Tarif register 14"));
        }

        // current
        put("1.1.204.8.0.255", new ValueFactory(5, 0, "Power downs") {
            Date getToTime() {
                return null;
            }

            ;
        });

        // current
        put("1.1.205.8.0.255", new ValueFactory(6, 0, "Power down time") {
            Date getToTime() {
                return null;
            }

            ;
        });

    }

    /**
     * @return list of all ObisCodes supported by the currently connected
     * meter.  Does this by trial and error.
     */
    private List getMeterSupportedObisCodes() throws IOException {
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
    class ValueFactory {
        boolean read = false;
        int address;
        int historic;

        ObisCode obisCode;
        String description;
        E120RegisterValue register;

        ValueFactory(String description) {
            this.description = description;
        }

        ValueFactory(int address, int historic, String description) {
            this.address = address;
            this.historic = historic;
            this.description = description;
        }

        Quantity getQuantity() throws IOException {
            return null;
        }

        // since the eventTime is always the same as the toTime ... shortcut
        Date getEventTime() throws IOException {
            return null;
        }

        Date getFromTime() throws IOException {
            return null;
        }

        ObisCode getObisCode() throws IOException {
            return obisCode;
        }

        Date getToTime() throws IOException {
            Calendar c = Calendar.getInstance(e120.getTimeZone());
            c.setTime(register.getTime());
            c.add(Calendar.DAY_OF_YEAR, 1);
            return c.getTime();
        }

        E120RegisterValue getRegister() throws IOException {
            if (!read) {
                read = true;
                Message msg = e120.getConnection().registerValue(address, historic);
                DefaultResponse response = (DefaultResponse) msg.getBody();
                if (!response.isOk()) {
                    return null;
                }
                register = ((E120RegisterValue) response.getValue());
            }
            return register;
        }

        RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
            if (getRegister() == null) {
                throwException(obisCode);
            }
            Quantity q = getRegister().toQuantity();
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
    static class ObisCodeWrapper implements Comparable<ObisCodeWrapper> {

        private ObisCode obisCode;

        private String os;
        private String reversedOs;

        ObisCodeWrapper(ObisCode oc) {
            obisCode = oc;

            os = obisCode.getA() + "." + obisCode.getB() + "." +
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
            return obisCode.toString();
        }

        public int compareTo(ObisCodeWrapper other) {
            return reversedOs.compareTo(other.reversedOs);
        }

    }

}