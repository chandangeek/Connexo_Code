/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.iec1107.abba1700.counters.PhaseFailureCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PhaseFailureCounter2;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PowerDownCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PowerDownCounter2;
import com.energyict.protocolimpl.iec1107.abba1700.counters.ProgrammingCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.ReverseRunCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.ReverseRunCounter2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.HistoricalValuesKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.PhaseFailureCounterKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.PhaseFailureCounterKey2;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.PowerDownCounterKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.PowerDownCounterKey2;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.ProgrammingCounterKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.ReverseRunCounterKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.ReverseRunCounterKey2;

public class ABBA1700MeterEvents {

    private final ABBA1700 protocol;
    private final ABBA1700MeterType meterType;

    /**
     * Create new instance
     *
     * @param protocol
     * @param meterType
     */
    public ABBA1700MeterEvents(final ABBA1700 protocol, final ABBA1700MeterType meterType) {
        this.protocol = protocol;
        this.meterType = meterType;
    }


    /**
     * Construct the list of meterEvents
     *
     * @param lastReading the date to start collection the meterEvents
     * @return the list of meterEvents
     */
    protected List<MeterEvent> getMeterEventList(final Date lastReading) {
        List<MeterEvent> meterEventList = new ArrayList<MeterEvent>();

        meterEventList.addAll(getPhaseFailureEvents(lastReading));
        meterEventList.addAll(getPowerDownEvents(lastReading));
        meterEventList.addAll(getProgrammingCounterEvents(lastReading));
        meterEventList.addAll(getReverseRunCounterEvents(lastReading));
        meterEventList.addAll(getBillingCounterEvents(lastReading));
        return meterEventList;
    }

    protected List<MeterEvent> getBillingCounterEvents(final Date lastReading) {
        List<MeterEvent> meterEventList = new ArrayList<MeterEvent>();
        try {
            HistoricalValueSetInfo hvsi = ((HistoricalValues) getABBA1700RegisterFactory().getRegister(HistoricalValuesKey)).getHistoricalValueSetInfo();
            if ((hvsi.getBillingStartDateTime() != null) && (hvsi.getBillingStartDateTime().after(lastReading))) {
                meterEventList.add(new MeterEvent(hvsi.getBillingStartDateTime(), MeterEvent.BILLING_ACTION, "Billing action source: " + ABBA1700MeterEvents.BillingEventTriggerSource.getTriggerReasonForValue(hvsi.getBillingTriggerSource())));
            }
        } catch (IOException e) {
            getLogger().info("Could not fetch the HistoricalValues for event support");
        }
        return meterEventList;
    }

    protected List<MeterEvent> getReverseRunCounterEvents(final Date lastReading) {
        List<MeterEvent> meterEventList = new ArrayList<MeterEvent>();
        try {

            if (meterType.getType() == ABBA1700MeterType.METERTYPE_32_TOU_5_CDR) {
                ReverseRunCounter2 rrc = (ReverseRunCounter2) getABBA1700RegisterFactory().getRegister(ReverseRunCounterKey2);
                if (rrc.getEventStart4().after(lastReading)) {
                    meterEventList.add(new MeterEvent(rrc.getEventStart4(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart3(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart2(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart1(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart0(), MeterEvent.REVERSE_RUN));
                } else if (rrc.getEventStart3().after(lastReading)) {
                    meterEventList.add(new MeterEvent(rrc.getEventStart3(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart2(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart1(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart0(), MeterEvent.REVERSE_RUN));
                } else if (rrc.getEventStart2().after(lastReading)) {
                    meterEventList.add(new MeterEvent(rrc.getEventStart2(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart1(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart0(), MeterEvent.REVERSE_RUN));
                } else if (rrc.getEventStart1().after(lastReading)) {
                    meterEventList.add(new MeterEvent(rrc.getEventStart1(), MeterEvent.REVERSE_RUN));
                    meterEventList.add(new MeterEvent(rrc.getEventStart0(), MeterEvent.REVERSE_RUN));
                } else if (rrc.getEventStart0().after(lastReading)) {
                    meterEventList.add(new MeterEvent(rrc.getEventStart0(), MeterEvent.REVERSE_RUN));
                }
            } else {
            ReverseRunCounter rrc = (ReverseRunCounter) getABBA1700RegisterFactory().getRegister(ReverseRunCounterKey);
            if (rrc.getThirdMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(rrc.getThirdMostRecentEventTime(), MeterEvent.REVERSE_RUN));
                meterEventList.add(new MeterEvent(rrc.getSecondMostRecentEventTime(), MeterEvent.REVERSE_RUN));
                meterEventList.add(new MeterEvent(rrc.getMostRecentEventTime(), MeterEvent.REVERSE_RUN));
            } else if (rrc.getSecondMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(rrc.getSecondMostRecentEventTime(), MeterEvent.REVERSE_RUN));
                meterEventList.add(new MeterEvent(rrc.getMostRecentEventTime(), MeterEvent.REVERSE_RUN));
            } else if (rrc.getMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(rrc.getMostRecentEventTime(), MeterEvent.REVERSE_RUN));
            }
            }
        } catch (IOException e) {
            getLogger().info("Could not fetch the reverseRunCounters");
        }
        return meterEventList;
    }

    protected List<MeterEvent> getProgrammingCounterEvents(final Date lastReading) {
        List<MeterEvent> meterEventList = new ArrayList<MeterEvent>();
        try {
            ProgrammingCounter pc = (ProgrammingCounter) getABBA1700RegisterFactory().getRegister(ProgrammingCounterKey);
            if (pc.getThirdMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(pc.getThirdMostRecentEventTime(), MeterEvent.CONFIGURATIONCHANGE));
                meterEventList.add(new MeterEvent(pc.getSecondMostRecentEventTime(), MeterEvent.CONFIGURATIONCHANGE));
                meterEventList.add(new MeterEvent(pc.getMostRecentEventTime(), MeterEvent.CONFIGURATIONCHANGE));
            } else if (pc.getSecondMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(pc.getSecondMostRecentEventTime(), MeterEvent.CONFIGURATIONCHANGE));
                meterEventList.add(new MeterEvent(pc.getMostRecentEventTime(), MeterEvent.CONFIGURATIONCHANGE));
            } else if (pc.getMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(pc.getMostRecentEventTime(), MeterEvent.CONFIGURATIONCHANGE));
            }
        } catch (IOException e) {
            getLogger().info("Could not fetch the programmingCounters");
        }
        return meterEventList;
    }

    protected List<MeterEvent> getPowerDownEvents(final Date lastReading) {
        List<MeterEvent> meterEventList = new ArrayList<MeterEvent>();
        try {

            if (meterType.getType() == ABBA1700MeterType.METERTYPE_32_TOU_5_CDR) {
                PowerDownCounter2 pdc = (PowerDownCounter2) getABBA1700RegisterFactory().getRegister(PowerDownCounterKey2);
                if (pdc.getEventStart4().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getEventStart4(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart3(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart2(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart1(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart0(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                } else if (pdc.getEventStart3().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getEventStart3(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart2(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart1(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart0(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                } else if (pdc.getEventStart2().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getEventStart2(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart1(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart0(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                } else if (pdc.getEventStart1().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getEventStart1(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getEventStart0(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                } else if (pdc.getEventStart0().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getEventStart0(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                }

            } else {
            PowerDownCounter pdc = (PowerDownCounter) getABBA1700RegisterFactory().getRegister(PowerDownCounterKey);
            if (pdc.getThirdMostRecentEventTime().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getThirdMostRecentEventTime(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getSecondMostRecentEventTime(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getMostRecentEventTime(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
            } else if (pdc.getSecondMostRecentEventTime().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getSecondMostRecentEventTime(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
                    meterEventList.add(new MeterEvent(pdc.getMostRecentEventTime(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
            } else if (pdc.getMostRecentEventTime().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pdc.getMostRecentEventTime(), MeterEvent.POWERDOWN, ABBA1700ProfileEntry.POWERDOWN));
            }
            }

        } catch (IOException e) {
            getLogger().info("Could not fetch the powerDownCounters");
        }
        return meterEventList;
    }

    protected List<MeterEvent> getPhaseFailureEvents(Date lastReading) {
        List<MeterEvent> meterEventList = new ArrayList<MeterEvent>();
        try {

            if (meterType.getType() == ABBA1700MeterType.METERTYPE_32_TOU_5_CDR) {
                PhaseFailureCounter2 pfc = (PhaseFailureCounter2) getABBA1700RegisterFactory().getRegister(PhaseFailureCounterKey2);

                if (pfc.getEventStart4().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pfc.getEventStart4(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase4()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart3(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase3()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart2(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase2()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart1(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase1()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart0(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase0()));
                } else if (pfc.getEventStart3().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pfc.getEventStart3(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase3()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart2(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase2()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart1(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase1()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart0(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase0()));
                } else if (pfc.getEventStart2().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pfc.getEventStart2(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase2()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart1(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase1()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart0(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase0()));
                } else if (pfc.getEventStart1().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pfc.getEventStart1(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase1()));
                    meterEventList.add(new MeterEvent(pfc.getEventStart0(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase0()));
                } else if (pfc.getEventStart0().after(lastReading)) {
                    meterEventList.add(new MeterEvent(pfc.getEventStart0(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFailedPhase0()));
                }

            } else {
            PhaseFailureCounter pfc = (PhaseFailureCounter) getABBA1700RegisterFactory().getRegister(PhaseFailureCounterKey);
            if (pfc.getThirdMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(pfc.getThirdMostRecentEventTime(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getThirdFailedPhase()));
                meterEventList.add(new MeterEvent(pfc.getSecondMostRecentEventTime(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getSecondFailedPhase()));
                meterEventList.add(new MeterEvent(pfc.getMostRecentEventTime(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFirstFailedPhase()));
            } else if (pfc.getSecondMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(pfc.getSecondMostRecentEventTime(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getSecondFailedPhase()));
                meterEventList.add(new MeterEvent(pfc.getMostRecentEventTime(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFirstFailedPhase()));
            } else if (pfc.getMostRecentEventTime().after(lastReading)) {
                meterEventList.add(new MeterEvent(pfc.getMostRecentEventTime(), MeterEvent.PHASE_FAILURE, "Phase " + pfc.getFirstFailedPhase()));
            }
            }

        } catch (IOException e) {
            getLogger().info("Could not fetch the phaseFailureCounters");
        }
        return meterEventList;
    }

    private ABBA1700RegisterFactory getABBA1700RegisterFactory() {
        return protocol.getABBA1700RegisterFactory();
    }

    private Logger getLogger() {
        return protocol.getLogger();
    }

    enum BillingEventTriggerSource {

        BillingDate(1, "Billing Date"),
        SeasonChange(2, "Season changed"),
        TarrifChange(4, "Tariff changed"),
        SerialComm(8, "Serial communication port"),
        OpticalCom(16, "Optical communication port"),
        PushButton(32, "Push button"),
        ExternalInput(64, "External input"),
        PowerUpProcess(128, "Power up process");


        private final int value;
        private final String reason;

        private BillingEventTriggerSource(final int value, final String reason) {
            this.value = value;
            this.reason = reason;
        }

        public static final String getTriggerReasonForValue(int value) {
            for (final BillingEventTriggerSource billingSource : values()) {
                if (billingSource.getValue() == value) {
                    return billingSource.getReason();
                }
            }
            return "Unknown";
        }

        private int getValue() {
            return this.value;
        }

        private String getReason() {
            return this.reason;
        }
    }
}
