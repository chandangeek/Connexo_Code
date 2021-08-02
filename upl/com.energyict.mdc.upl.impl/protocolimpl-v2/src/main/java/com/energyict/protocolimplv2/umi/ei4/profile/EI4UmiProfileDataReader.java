package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.ei4.EI4Umi;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.ei4.messages.EI4UmiMessaging;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjPartRspPayload;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjRspPayload;
import com.energyict.protocolimplv2.umi.types.ResultCode;
import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EI4UmiProfileDataReader {
    public static final UmiCode UMI_WAN_PROFILE_STATUS = UmiCode.fromString("umi.1.1.194.22");
    public static final UmiCode UMI_WAN_PROFILE_TABLE = UmiCode.fromString("umi.1.1.194.23");
    public static final UmiCode UMI_WAN_PROFILE_CONTROL = UmiCode.fromString("umi.1.1.194.24");

    public static final ObisCode HALF_HOUR_LOAD_PROFILE = ObisCode.fromString("7.0.99.99.1.255");
    public static final String BULK_GAS_VOL_COMP_CHANNEL_OBIS_CODE = "7.0.3.0.0.255";
    public static final String BULK_GAS_VOL_COMP_CHANNEL_UNIT = "m3";

    public static final int QUANTITY_OF_READINGS_PER_READ = 10;
    public static final int LOAD_PROFILE_INTERVAL_SECONDS = 1800;

    private final EI4Umi protocol;
    private final List<ObisCode> supportedLoadProfiles;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final OfflineDevice offlineDevice;

    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private Map<ObisCode, Integer> intervalMap;

    public EI4UmiProfileDataReader(EI4Umi protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                                   OfflineDevice offlineDevice) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.offlineDevice = offlineDevice;
        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(HALF_HOUR_LOAD_PROFILE);
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(
                    lpr.getProfileObisCode(), offlineDevice.getDeviceIdentifier(), offlineDevice.getSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos = new ArrayList<>();
                ChannelInfo channelInfo = new ChannelInfo(0, BULK_GAS_VOL_COMP_CHANNEL_OBIS_CODE,
                        Unit.get(BULK_GAS_VOL_COMP_CHANNEL_UNIT), protocol.getSerialNumber());
                channelInfo.setMultiplier(new BigDecimal("0.01"));
                channelInfos.add(channelInfo);
                getChannelInfosMap().put(lpr, channelInfos);
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(LOAD_PROFILE_INTERVAL_SECONDS);
            } else {
                lpc.setSupportedByMeter(false);
            }
            result.add(lpc);
        }
        return result;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile =
                    collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(
                            loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(),
                            offlineDevice.getDeviceIdentifier()));
            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            if (isSupported(loadProfileReader) && (channelInfos != null)) {
                try {
                    EI4UmiwanProfileStatus status = getProfileStatus();
                    int entriesNumber = status.getNumberOfEntries();
                    long startTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(status.getStartTime().getTime());
                    long nextReadingBlockStartSeconds = TimeUnit.MILLISECONDS.toSeconds(loadProfileReader.getStartReadingTime().getTime());

                    boolean rightStartTime = (startTimeSeconds <= nextReadingBlockStartSeconds)
                            && (startTimeSeconds != TimeUnit.MILLISECONDS.toSeconds(UmiHelper.UMI_ZERO_DATE.getTime()));

                    List<EI4UmiMeterReading> collectedUmiReadings = new ArrayList<>();
                    if (rightStartTime) {
                        int loops = entriesNumber / QUANTITY_OF_READINGS_PER_READ;
                        int remainder = entriesNumber % QUANTITY_OF_READINGS_PER_READ;
                        for (int i = 0; i <= loops; i++) {
                            boolean lastCycle = loops == i;
                            if (lastCycle) {
                                if (remainder == 0) {
                                    continue;
                                }
                            }
                            String lastPartOfCode = lastCycle ?
                                    ("[" + (i * QUANTITY_OF_READINGS_PER_READ) + ":" + (i * QUANTITY_OF_READINGS_PER_READ + remainder - 1) + "]")
                                    : ("[" + (i * QUANTITY_OF_READINGS_PER_READ) + ":" + (i * QUANTITY_OF_READINGS_PER_READ + QUANTITY_OF_READINGS_PER_READ - 1) + "]");
                            Pair<ResultCode, ReadObjPartRspPayload> pair = protocol.getUmiSession().readObjectPart(new UmiObjectPart(UMI_WAN_PROFILE_TABLE.toString() + lastPartOfCode));
                            if (pair.getFirst() != ResultCode.OK) {
                                getProtocol().journal(Level.WARNING, "Read meter reading operation failed." + pair.getFirst().getDescription());
                                throw new ProtocolException("Reading of umiwan profile table " + UMI_WAN_PROFILE_TABLE.getCode() + lastPartOfCode + "failed. " + pair.getFirst().getDescription());

                            } else {
                                collectedUmiReadings.addAll(getReadings(pair.getLast().getValue(), lastCycle ? remainder : QUANTITY_OF_READINGS_PER_READ));
                            }
                        }
                        if (!collectedUmiReadings.isEmpty()) {
                            List<IntervalData> intervalData = getIntervalData(collectedUmiReadings);
                            collectedLoadProfile.setCollectedIntervalData(intervalData, channelInfos);
                            collectedLoadProfile.setDoStoreOlderValues(true);
                        }
                    } else {
                        String message = "Start time mismatch. No readings can be collected. Profile control correction is required.";
                        getProtocol().journal(Level.WARNING, message);
                        Issue problem = issueFactory.createWarning(loadProfileReader, "loadProfileXBlockingIssue",
                                loadProfileReader.getProfileObisCode(), message);
                        collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete, problem);
                    }

                    int size = collectedUmiReadings.size();
                    Date dateToSet = rightStartTime && size > 0 ? collectedUmiReadings.get(size - 1).getTimestamp() : loadProfileReader.getStartReadingTime();
                    sendProfileControl(dateToSet);

                } catch (Exception e) {
                    Issue problem = issueFactory.createWarning(loadProfileReader, "loadProfileXBlockingIssue",
                            loadProfileReader.getProfileObisCode(), e.getMessage());
                    collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete, problem);
                }
            } else {
                Issue problem = issueFactory.createWarning(loadProfileReader, "loadProfileXnotsupported",
                        loadProfileReader.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            result.add(collectedLoadProfile);
        }
        return result;
    }

    private List<EI4UmiMeterReading> getReadings(byte[] value, int q) {
        int l = value.length / q;
        List<EI4UmiMeterReading> list = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            byte[] el = new byte[l];
            for (int j = 0; j < l; j++) {
                el[j] = value[i * l + j];
            }
            list.add(new EI4UmiMeterReading(el));
        }
        return list;
    }

    private EI4UmiwanProfileStatus getProfileStatus() throws IOException, GeneralSecurityException {
        Pair<ResultCode, ReadObjRspPayload> pairSt = protocol.getUmiSession().readObject(UMI_WAN_PROFILE_STATUS);
        if (pairSt.getFirst() != ResultCode.OK) {
            getProtocol().journal(Level.WARNING, "Read umiwan profile status operation failed. " + pairSt.getFirst().getDescription());
            throw new ProtocolException("Reading of umiwan profile status " + UMI_WAN_PROFILE_STATUS.getCode() + " failed. " + pairSt.getFirst().getDescription());
        }
        EI4UmiwanProfileStatus status = new EI4UmiwanProfileStatus(pairSt.getLast().getValue());
        getProtocol().journal(Level.INFO, "Profile status start time: " + status.getStartTime());
        getProtocol().journal(Level.INFO, "Profile status number of readings: " + status.getNumberOfEntries());
        return status;
    }

    private EI4UmiwanProfileControl sendProfileControl(Date dateToSet) throws IOException, GeneralSecurityException {
        EI4UmiwanProfileControl control = new EI4UmiwanProfileControl(dateToSet, 0);
        ResultCode resultCode = protocol.getUmiSession().writeObject(UMI_WAN_PROFILE_CONTROL, control.getRaw());
        if (resultCode != ResultCode.OK) {
            getProtocol().journal(Level.WARNING, "Set profile control operation failed. " + resultCode.getDescription());
            throw new ProtocolException("Setting of Umiwan profile control " + UMI_WAN_PROFILE_CONTROL.getCode() + "failed. " + resultCode.getDescription());
        }
        getProtocol().journal(Level.INFO, "Profile control has been set to " + dateToSet);
        return control;
    }

    public EI4UmiwanProfileControl setUmiwanProfileControl(OfflineDeviceMessage pendingMessage) {
        try {
            Date date = pendingMessage.getDeviceMessageAttributes().stream()
                    .filter(offlineDeviceMessageAttribute -> offlineDeviceMessageAttribute.getName().equals("startTime"))
                    .map(OfflineDeviceMessageAttribute::getValue)
                    .map(value -> {
                        try {
                            return EI4UmiMessaging.europeanDateTimeFormat.parse(value);
                        } catch (ParseException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Start time argument is missing or invalid"));
            return sendProfileControl(date);
        } catch (Exception e) {
            getProtocol().journal(Level.WARNING, "Set profile control operation failed.", e);
            throw ConnectionCommunicationException.unExpectedProtocolError(new IOException(e.getMessage()));
        }
    }

    public EI4Umi getProtocol() {
        return this.protocol;
    }

    private List<IntervalData> getIntervalData(List<EI4UmiMeterReading> table) {
        List<IntervalData> intervalData = new ArrayList<>();
        for (EI4UmiMeterReading reading : table) {
            IntervalData data = new IntervalData(reading.getTimestamp());
            List<IntervalValue> values = new ArrayList<>();
            int protocolStatusFlag = reading.getStatusFlags();

            try {
                values.add(new IntervalValue(reading.getReading(), protocolStatusFlag,
                        EI4UmiReadingStatusFlagMapper.map(protocolStatusFlag)));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            data.setIntervalValues(values);
            intervalData.add(data);
        }
        return intervalData;
    }

    private boolean isSupported(LoadProfileReader lpr) {
        for (ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (lpr.getProfileObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    private Map<ObisCode, Integer> getIntervalMap() {
        if (intervalMap == null) {
            intervalMap = new HashMap<>();
        }
        return intervalMap;
    }

}
