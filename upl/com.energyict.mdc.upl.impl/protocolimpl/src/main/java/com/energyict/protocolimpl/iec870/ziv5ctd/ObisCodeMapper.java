/*
 * ObisCodeMapper.java
 *
 * Created on 21 December 2005, 14:22
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * So in the case of input:
 * Active import = Incremental active energy
 * Active export = 0
 * Q1 = Incremental Inductive reactive
 * Q2 = 0
 * Q3 = 0
 * Q4 = Incremental Capacitive reactive
 * <p>
 * In the case of output:
 * Active import = 0
 * Active export = Incremental active energy
 * Q1 = 0
 * Q2 = Incremental Capacitive reactive
 * Q3 = Incremental Inductive reactive
 * Q4 = 0
 *
 * @author fbo
 */

public class ObisCodeMapper {

    private Ziv5Ctd ziv = null;
    private RegisterFactory rFactory = null;

    /**
     * Collection for sorting the keys
     */
    private List<ObisCodeWrapper> keys = new ArrayList<>();
    /**
     * HashMap with the ValueFactories per ObisCode
     */
    private Map<ObisCodeWrapper, ValueFactory> oMap = new HashMap<>();

    /**
     * Creates a new instance of ObisCodeMapping
     */
    ObisCodeMapper(Ziv5Ctd ziv, RegisterFactory registerFactory) {
        this.ziv = ziv;
        this.rFactory = registerFactory;
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

    /**
     * @return construct extended logging
     */
    public String getExtendedLogging() throws IOException {
        StringBuilder builder = new StringBuilder();
        List<ObisCode> obisList = getMeterSupportedObisCodes();
        for (ObisCode obc : obisList) {
            builder.append(obc.toString()).append(" ").append(getRegisterInfo(obc)).append("\n");
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
            builder.append(key).append(" ").append(vf.toString()).append("\n");
        }
        return builder.toString();
    }

    /**
     * This is the init for the actual values, this method does not
     * read any register configuration information, since that requires
     * communication.
     */
    private void init() {

        ObisCode o = null;

        // create obiscodes for time register
        o = ObisCode.fromString("1.1.0.1.2.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() {
                return null;
            }

            RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
                return new RegisterValue(obisCode, ziv.getTime());
            }
        });

        // create obiscodes for cummulative registers
        o = ObisCode.fromString("1.1.1.9.0.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() throws IOException {
                return rFactory.getInformationObjectC0().getActiveImport();
            }
        });

        o = ObisCode.fromString("1.1.2.9.0.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() throws IOException {
                return rFactory.getInformationObjectC0().getActiveExport();
            }
        });

        o = ObisCode.fromString("1.1.5.9.0.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() throws IOException {
                return rFactory.getInformationObjectC0().getQ1();
            }
        });

        o = ObisCode.fromString("1.1.6.9.0.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() throws IOException {
                return rFactory.getInformationObjectC0().getQ2();
            }
        });

        o = ObisCode.fromString("1.1.7.9.0.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() throws IOException {
                return rFactory.getInformationObjectC0().getQ3();
            }
        });

        o = ObisCode.fromString("1.1.8.9.0.255");
        put(o, new ValueFactory(o) {
            Quantity getQuantity() throws IOException {
                return rFactory.getInformationObjectC0().getQ4();
            }
        });

        ///////////////////////////////////////////////////////////////////////

        int[] contract = {128, 129, 130};
        for (int c = 0; c < contract.length; c++) {
            final String cDescription = ", Contract " + (c + 1);
            final int ci = c;

            int[] rate = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            for (int r = 0; r < rate.length; r++) {

                final int ri = r;

                // Active Input
                o = ObisCode.fromString("1." + contract[c] + ".1.9." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kWh");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getIncrementalA();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Active Input
                o = ObisCode.fromString("1." + contract[c] + ".1.8." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kWh");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getAbsoluteA();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Active Export
                o = ObisCode.fromString("1." + contract[c] + ".2.9." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kWh");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getIncrementalA();
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Active Export
                o = ObisCode.fromString("1." + contract[c] + ".2.8." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kWh");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getAbsoluteA();
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Q1
                o = ObisCode.fromString("1." + contract[c] + ".5.9." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getIncrementalRi();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Q1
                o = ObisCode.fromString("1." + contract[c] + ".5.8." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getAbsoluteRi();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Q2
                o = ObisCode.fromString("1." + contract[c] + ".6.9." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getIncrementalRc();
                        }
                    }

                    public String toString() {
                        return this.obisCode.toString() + cDescription;
                    }
                });

                // Q2
                o = ObisCode.fromString("1." + contract[c] + ".6.8." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getAbsoluteRc();
                        }
                    }

                    public String toString() {
                        return this.obisCode.toString() + cDescription;
                    }
                });

                // Q3
                o = ObisCode.fromString("1." + contract[c] + ".7.9." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getIncrementalRi();
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Q3
                o = ObisCode.fromString("1." + contract[c] + ".7.8." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getAbsoluteRi();
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Q4
                o = ObisCode.fromString("1." + contract[c] + ".8.9." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getIncrementalRc();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Q4
                o = ObisCode.fromString("1." + contract[c] + ".8.8." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kvar");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getAbsoluteRc();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Max
                o = ObisCode.fromString("1." + contract[c] + ".1.6." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kWh");
                        if (isImport) {
                            return rFactory.getInfo87(ci).getPeriod(ri).getMaxPotentia();
                        } else {
                            return new Quantity(new BigDecimal(0), unit);
                        }
                    }

                    Date getEventTime() throws IOException {
                        return rFactory.getInfo87(ci).getPeriod(ri).getMaxPotentiaDate();
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

                // Max
                o = ObisCode.fromString("1." + contract[c] + ".2.6." + rate[r] + ".255");
                put(o, new ValueFactory(o) {
                    Quantity getQuantity() throws IOException {
                        if (rFactory.getInfo87(ci) != null && rFactory.getInfo87(ci).getPeriod(ri) == null) {
                            throwException(obisCode);
                        }
                        boolean isImport = rFactory.getInfoObject96(ci).isImport();
                        Unit unit = Unit.get("kWh");
                        if (isImport) {
                            return new Quantity(new BigDecimal(0), unit);
                        } else {
                            return rFactory.getInfo87(ci).getPeriod(ri).getMaxPotentia();
                        }
                    }

                    Date getEventTime() throws IOException {
                        return rFactory.getInfo87(ci).getPeriod(ri).getMaxPotentiaDate();
                    }

                    public String toString() {
                        return obisCode.toString() + cDescription;
                    }
                });

            }

        }

        for (int bpi = 0; bpi < 15; bpi++) {
            final int bp = bpi;

            for (int c = 0; c < contract.length; c++) {
                final String cDescription = ", Contract " + (c + 1);
                final int ci = c;

                int[] rate = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                for (int r = 0; r < rate.length; r++) {
                    final int ri = r;

                    // Time
                    o = ObisCode.fromString("1.1.0.1.2." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() {
                            return null;
                        }

                        RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
                            return new RegisterValue(obisCode, getI88p().getEndPeriod());
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });


                    // Active Input
                    o = ObisCode.fromString("1." + contract[c] + ".1.9." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kWh");
                            if (isImport) {
                                return getI88p().getIncrementalA();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Active Input
                    o = ObisCode.fromString("1." + contract[c] + ".1.8." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kWh");
                            if (isImport) {
                                return getI88p().getAbsoluteA();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Active Export
                    o = ObisCode.fromString("1." + contract[c] + ".2.9." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kWh");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return getI88p().getIncrementalA();
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Active Export
                    o = ObisCode.fromString("1." + contract[c] + ".2.8." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kWh");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return getI88p().getAbsoluteA();
                            }
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Q1
                    o = ObisCode.fromString("1." + contract[c] + ".5.9." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return getI88p().getIncrementalRi();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Q1
                    o = ObisCode.fromString("1." + contract[c] + ".5.8." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return getI88p().getAbsoluteRi();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Q2
                    o = ObisCode.fromString("1." + contract[c] + ".6.9." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return getI88p().getIncrementalRc();
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return this.obisCode.toString() + cDescription;
                        }
                    });

                    // Q2
                    o = ObisCode.fromString("1." + contract[c] + ".6.8." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {

                            InformationObject88Period i88p = null;
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    (i88p = rFactory.getInfoObject88(ci).get(bp).getPeriod(ri)) == null) {
                                throwException(obisCode);
                            }

                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return i88p.getAbsoluteRc();
                            }
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return this.obisCode.toString() + cDescription;
                        }
                    });

                    // Q3
                    o = ObisCode.fromString("1." + contract[c] + ".7.9." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {

                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return getI88p().getIncrementalRi();
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Q3
                    o = ObisCode.fromString("1." + contract[c] + ".7.8." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return getI88p().getAbsoluteRi();
                            }
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Q4
                    o = ObisCode.fromString("1." + contract[c] + ".8.9." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return getI88p().getIncrementalRc();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Q4
                    o = ObisCode.fromString("1." + contract[c] + ".8.8." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {

                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kvar");
                            if (isImport) {
                                return getI88p().getAbsoluteRc();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Max
                    o = ObisCode.fromString("1." + contract[c] + ".1.6." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {

                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kWh");
                            if (isImport) {
                                return getI88p().getMaxPotentia();
                            } else {
                                return new Quantity(new BigDecimal(0), unit);
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        Date getEventTime() throws IOException {
                            return getI88p().getMaxPotentiaDate();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });

                    // Max
                    o = ObisCode.fromString("1." + contract[c] + ".2.6." + rate[r] + "." + bpi);
                    put(o, new ValueFactory(o) {

                        InformationObject88Period getI88p() throws IOException {
                            if (rFactory.getInfoObject88(ci) == null ||
                                    rFactory.getInfoObject88(ci).get(bp) == null ||
                                    rFactory.getInfoObject88(ci).get(bp).getPeriod(ri) == null) {
                                throwException(obisCode);
                            }
                            return rFactory.getInfoObject88(ci).get(bp).getPeriod(ri);
                        }

                        Quantity getQuantity() throws IOException {
                            boolean isImport = rFactory.getInfoObject96(ci).isImport();
                            Unit unit = Unit.get("kWh");
                            if (isImport) {
                                return new Quantity(new BigDecimal(0), unit);
                            } else {
                                return getI88p().getMaxPotentia();
                            }
                        }

                        Date getFromTime() throws IOException {
                            return getI88p().getStartPeriod();
                        }

                        Date getToTime() throws IOException {
                            return getI88p().getEndPeriod();
                        }

                        Date getEventTime() throws IOException {
                            return getI88p().getMaxPotentiaDate();
                        }

                        public String toString() {
                            return obisCode.toString() + cDescription;
                        }
                    });
                }
            }
        }
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

        // since the eventTime is always the same as the toTime ... shortcut
        Date getEventTime() throws IOException {
            return null;
        }

        ;

        Date getFromTime() throws IOException {
            return null;
        }

        ;

        Date getToTime() throws IOException {
            return new Date();
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
            return "ObisCode: " + obisCode;
        }

        public int compareTo(ObisCodeWrapper other) {
            return reversedOs.compareTo(other.reversedOs);
        }

    }

}