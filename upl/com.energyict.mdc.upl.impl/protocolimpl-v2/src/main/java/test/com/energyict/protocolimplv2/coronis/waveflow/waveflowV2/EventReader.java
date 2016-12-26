package test.com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.ApplicationStatusParser;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.EventStatusAndDescription;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.BackflowDetectionFlags;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.ProfileType;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.PulseWeight;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.BackFlowEventByFlowRate;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.BackFlowEventByVolumeMeasuring;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.LeakageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/06/13
 * Time: 13:58
 * Author: khe
 */
public class EventReader implements DeviceLogBookSupport {

    private final WaveFlowV2 waveFlowV2;
    private final CollectedDataFactory collectedDataFactory;

    public EventReader(WaveFlowV2 waveFlowV2, CollectedDataFactory collectedDataFactory) {
        this.waveFlowV2 = waveFlowV2;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        LogBookReader logBookReader = logBooks.get(0);
        Date lastLogBook = logBookReader.getLastLogBook();
        CollectedLogBook result = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());

        List<MeterEvent> meterEvents = buildMeterEvents(false);
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();

        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getTime().after(lastLogBook)) {
                meterProtocolEvents.add(MeterEvent.mapMeterEventToMeterProtocolEvent(meterEvent));
            }
        }
        result.setCollectedMeterEvents(meterProtocolEvents);
        return Collections.singletonList(result);
    }

    public List<MeterEvent> buildMeterEvents(boolean bubbleUpOrigin) {

        List<MeterEvent> meterEvents = new ArrayList<>();
        EventStatusAndDescription translator = new EventStatusAndDescription(waveFlowV2);

        boolean usesInitialRFCommand = waveFlowV2.getWaveFlowProperties().usesInitialRFCommand();
        if (!usesInitialRFCommand) {
            //Check the profile type. This defines the module's extra functionality, eg. backflow and reed fault detection, and thus the events to read out!
            ProfileType profileType = waveFlowV2.getParameterFactory().readProfileType();

            //Simple backflow detection for the past 12 months, for input channel A (0) and B (1)
            if (profileType.supportsSimpleBackflowDetection()) {
                for (int input = 0; input <= 1; input++) {
                    BackflowDetectionFlags backflowDetectionFlags = waveFlowV2.getParameterFactory().readSimpleBackflowDetectionFlags(input);  //0 = channel A, 1 = channel B
                    for (int i = 0; i <= 12; i++) {
                        if (backflowDetectionFlags.flagIsSet(i)) {
                            Date eventDate = backflowDetectionFlags.getEventDate(i);
                            meterEvents.add(new MeterEvent(eventDate, MeterEvent.METER_ALARM, translator.getProtocolCodeForSimpleBackflow(input), "Backflow detected on input " + backflowDetectionFlags.getInputChannelName()));
                        }
                    }
                }
            }

            //Advanced backflow detection
            if (profileType.supportsAdvancedBackflowDetection()) {
                //Detection by measuring water volume
                if (waveFlowV2.getParameterFactory().readExtendedOperationMode().usingVolumeMethodForBackFlowDetection()) {
                    for (BackFlowEventByVolumeMeasuring backFlowEvent : waveFlowV2.getRadioCommandFactory().readBackFlowEventTableByVolumeMeasuring().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV2.getPulseWeight(inputIndex);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartOfDetectionDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex, true), "Backflow start, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndOfDetectionDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex, false), "Backflow end, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                    }
                }

                //Detection by flow rate
                else {
                    for (BackFlowEventByFlowRate backFlowEvent : waveFlowV2.getRadioCommandFactory().readBackFlowEventTableByFlowRate().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV2.getPulseWeight(inputIndex);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex, true), "Backflow start, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex, false), "Backflow end, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                    }
                }
            }

            for (LeakageEvent leakageEvent : waveFlowV2.getRadioCommandFactory().readLeakageEventTable().getLeakageEvents()) {
                if (leakageEvent.isValid()) {
                    String startOrEnd = leakageEvent.getStatusDescription();
                    String leakageType = leakageEvent.getLeakageType();
                    String inputChannel = leakageEvent.getCorrespondingInputChannel();
                    if (leakageEvent.getLeakageType().equals(LeakageEvent.LEAKAGETYPE_EXTREME)) {
                        meterEvents.add(new MeterEvent(leakageEvent.getDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(startOrEnd, leakageType, inputChannel), startOrEnd + " of " + leakageEvent.getEventDescription() + " on input " + inputChannel + ": flow-rate = " + leakageEvent.getConsumptionRate()));
                    }
                    if (leakageEvent.getLeakageType().equals(LeakageEvent.LEAKAGETYPE_RESIDUAL)) {
                        meterEvents.add(new MeterEvent(leakageEvent.getDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(startOrEnd, leakageType, inputChannel), startOrEnd + " of " + leakageEvent.getEventDescription() + " on input " + inputChannel + ": flow-rate = " + leakageEvent.getConsumptionRate()));
                    }
                }
            }
        }

        //Parse the application status for events
        int applicationStatus = waveFlowV2.getParameterFactory().readApplicationStatus();
        boolean valve = false;
        if (!usesInitialRFCommand) {
            valve = waveFlowV2.getParameterFactory().readProfileType().supportsWaterValveControl();
        }
        ApplicationStatusParser parser = new ApplicationStatusParser(waveFlowV2, bubbleUpOrigin);
        meterEvents.addAll(parser.getMeterEvents(usesInitialRFCommand, applicationStatus, valve));

        return meterEvents;
    }
}