package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29.09.15
 * Time: 10:08
 */
public class AM540LoadProfileBuilder extends LGLoadProfileBuilder {

    private IDISProfileDataReader idisProfileDataReader;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public AM540LoadProfileBuilder(AbstractDlmsProtocol meterProtocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, boolean supportsBulkRequests, CollectedDataFactory collectedDataFactory) {
        super(meterProtocol, issueService, readingTypeUtilService, supportsBulkRequests, collectedDataFactory);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> allLoadProfileReaders) {
        List<LoadProfileReader> eMeterLoadProfileReaders = new ArrayList<>();
        List<LoadProfileReader> mbusLoadProfileReaders = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : allLoadProfileReaders) {
            if (loadProfileReader.getMeterSerialNumber().equals(getMeterProtocol().getSerialNumber())) {
                eMeterLoadProfileReaders.add(loadProfileReader);
            } else {
                mbusLoadProfileReaders.add(loadProfileReader);
            }
        }

        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(allLoadProfileReaders.size());
        loadProfileConfigurations.addAll(super.fetchLoadProfileConfiguration(eMeterLoadProfileReaders));
        loadProfileConfigurations.addAll(fetchMBusLoadProfileConfiguration(mbusLoadProfileReaders));
        return loadProfileConfigurations;
    }

    private List<CollectedLoadProfileConfiguration> fetchMBusLoadProfileConfiguration(List<LoadProfileReader> mbusLoadProfileReaders) {
        return getMbusProfileDataReader().fetchLoadProfileConfiguration(mbusLoadProfileReaders);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<LoadProfileReader> eMeterLoadProfileReaders = new ArrayList<>();
        List<LoadProfileReader> mbusLoadProfileReaders = new ArrayList<>();

        for (LoadProfileReader loadProfile : loadProfiles) {
            if (loadProfile.getMeterSerialNumber().equals(getMeterProtocol().getSerialNumber())) {
                eMeterLoadProfileReaders.add(loadProfile);
            } else {
                mbusLoadProfileReaders.add(loadProfile);
            }
        }

        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<>(loadProfiles.size());
        collectedLoadProfileList.addAll(super.getLoadProfileData(eMeterLoadProfileReaders));
        collectedLoadProfileList.addAll(getMBusLoadProfileData(mbusLoadProfileReaders));
        return collectedLoadProfileList;
    }

    private List<CollectedLoadProfile> getMBusLoadProfileData(List<LoadProfileReader> mbusLoadProfileReaders) {
        return getMbusProfileDataReader().getLoadProfileData(mbusLoadProfileReaders);
    }

    private IDISProfileDataReader getMbusProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new AM540MbusProfileDataReader(getMeterProtocol(), getCollectedDataFactory(), getIssueService());
        }
        return idisProfileDataReader;
    }
}
