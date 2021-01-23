package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectedLoadProfileReader  {

    private final List<com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader> specificReadableRegisters;
    private final AbstractDlmsProtocol dlmsProtocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public CollectedLoadProfileReader(List<com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader> specificReadableRegisters, AbstractDlmsProtocol dlmsProtocol, CollectedDataFactory collectedLoadProfileBuilder, IssueFactory issueFactory) {
        this.specificReadableRegisters = specificReadableRegisters;
        this.collectedDataFactory = collectedLoadProfileBuilder;
        this.dlmsProtocol = dlmsProtocol;
        this.issueFactory = issueFactory;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> loadProfiles = new ArrayList<>(loadProfileReaders.size());
        for (LoadProfileReader lpr : loadProfileReaders) {
            Optional<com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader> optionalReader = findReader(lpr.getProfileObisCode());
            if (optionalReader.isPresent()) {
                loadProfiles.add(optionalReader.get().read(dlmsProtocol, lpr));
            } else {
                CollectedLoadProfile collectedLoadProfile = collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode(), dlmsProtocol.getOfflineDevice().getDeviceIdentifier()));
                Issue problem = issueFactory.createWarning(lpr, "loadProfile not supported", lpr.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
        }
        return loadProfiles;
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        for (LoadProfileReader lpr : loadProfileReaders) {
            Optional<com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader> foundReader = findReader(lpr.getProfileObisCode());
            if (foundReader.isPresent()) {
                result.add(foundReader.get().getChannelInfo(lpr, dlmsProtocol));
            } else {
                CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), dlmsProtocol.getOfflineDevice().getDeviceIdentifier(), lpr.getMeterSerialNumber());
                lpc.setSupportedByMeter(false);
                result.add(lpc);
            }
        }
        return result;
    }


    private Optional<com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader> findReader(ObisCode profileObisCode) {
        return specificReadableRegisters.stream().filter(f -> f.isApplicable(profileObisCode)).findFirst();
    }
}
