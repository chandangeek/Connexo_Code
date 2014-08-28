package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.common.ApplicationException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DialerFactory implements Comparable, Serializable {

    /* Examples of usage:
      *** direct rs232 connection ***
      dialer =DialerFactory.getDirectDialer().newDialer();
      dialer.init("COM1");
      dialer.connect("",60000);

    */
    private static String ATDIALER = "ATDIALER";
    private static String IPDIALER = "IPDIALER";
    private static String IPDIALERSELECTOR = "IPDIALERSELECTOR";
    private static String PAKNETDIALER = "PAKNETDIALER";
    private static String PEMPDIALER = "PEMPDIALER";
    private static String EMDIALDIALER = "EMDIALDIALER";
    private static String DIRECTDIALER = "DIRECTDIALER";
    private static String OPTICALDIALER = "OPTICALDIALER";
    private static String NULLDIALER = "NULLDIALER";
    private static String CASEDIALER = "CASEDIALER";
    private static String EXTENDED_ATDIALER = "EXTENDEDATDIALER";

    /**
     * Dialer implementation that creates a "virtual" connection over an RF network.
     */
    private static final String CYNET_DIALER = "CYNETRFDIALER";

    /**
     * Dialer implementation that creates a "virtual" connection over an RF network.
     */
    private static final String WAVENIS_DIALER = "WAVENISRFDIALER";

    private static DialerFactory[] all = {
            new DialerFactory(ATDIALER, "com.energyict.dialer.coreimpl.ATDialer"),
            new DialerFactory(DIRECTDIALER, "com.energyict.dialer.coreimpl.DirectDialer"),
            new DialerFactory(OPTICALDIALER, "com.energyict.dialer.coreimpl.OpticalDialer"),
            new DialerFactory(IPDIALER, "com.energyict.dialer.coreimpl.IPDialer"),
            new DialerFactory(IPDIALERSELECTOR, "com.energyict.dialer.coreimpl.IPDialerSelector"),
            new DialerFactory(PAKNETDIALER, "com.energyict.dialer.coreimpl.PAKNETDialer"),
            new DialerFactory(PEMPDIALER, "com.energyict.dialer.coreimpl.PEMPDialer"),
            new DialerFactory(EMDIALDIALER, "com.energyict.dialer.coreimpl.EMDialDialer"),
            new DialerFactory(NULLDIALER, "com.energyict.dialer.coreimpl.NullDialer"),
            new DialerFactory(CASEDIALER, "com.energyict.dialer.coreimpl.CaseDialer"),
            new DialerFactory(CYNET_DIALER, "com.energyict.concentrator.communication.dialer.rf.cynet.CynetRFDialer"),
            new DialerFactory(WAVENIS_DIALER, "com.energyict.concentrator.communication.dialer.rf.wavenis.WavenisRFDialer"),
            new DialerFactory(EXTENDED_ATDIALER, "com.energyict.dialer.coreimpl.ExtendedATDialer"),
    };

    private static DialerFactory EMPTYDIALER = new DialerFactory(null, "");

    private String name;
    private String dialerClassName;

    private DialerFactory(String name, String dialerClassName) {
        this.name = name;
        this.dialerClassName = dialerClassName;
    }

    public String getDialerClassName() {
        return dialerClassName;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        if (getDialerClassName().isEmpty()) {
            return "";
        }
        return getName();
    }

    public String toString() {
        return getLocalizedName();
    }

    public Dialer newDialer() {
        try {
            if (getDialerClassName().isEmpty()) {
                return null;
            }
            return (Dialer) Class.forName(getDialerClassName()).newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            throw new ApplicationException(ex);
        }
    }

    // Comparable interface

    public int compareTo(Object o) {
        return ((DialerFactory) o).getDialerClassName().compareTo(getDialerClassName());
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            DialerFactory input = (DialerFactory) o;
            if (isEmptyDialer(input)) {
                return isEmptyDialer(this);
            } else if (isEmptyDialer(this)) {
                return false;
            }
            return input.getDialerClassName().equals(getDialerClassName());
        } catch (ClassCastException x) {
            return false;
        }
    }

    public int hashCode() {
        if (isEmptyDialer(this)) {
            return 21;
        } else {
            return getDialerClassName().hashCode();
        }
    }

    public static DialerFactory getDefault() {
        return all[0];
    }

    public static DialerFactory get(String name) {
        if ((name == null) || name.isEmpty() || ("none".compareTo(name) == 0)) {
            return getEmptyDialer();
        }
        for (int i = 0; i < all.length; i++) {
            if (all[i].getName().equals(name)) {
                return all[i];
            }
        }
        throw new ApplicationException("Dialer type " + name + " does not exist");
    }

    public static DialerFactory get(int index) {
        if (index >= all.length) {
            return null;
        } else {
            return all[index];
        }
    }

    public static int nrOfDialers() {
        return all.length;
    }

    public static List<DialerFactory> getAll() {
        return Collections.unmodifiableList(Arrays.asList(all));
    }

    public static DialerFactory getDirectDialer() {
        return DialerFactory.get(DIRECTDIALER);
    }

    public static DialerFactory getOpticalDialer() {
        return DialerFactory.get(OPTICALDIALER);
    }

    public static DialerFactory getEmptyDialer() {
        return EMPTYDIALER;
    }

    public static DialerFactory getStandardModemDialer() {
        return DialerFactory.get(ATDIALER);
    }

    public static boolean isEmptyDialer(DialerFactory fact) {
        return (fact != null && fact.getName() == null && fact.getDialerClassName().isEmpty());
    }

}