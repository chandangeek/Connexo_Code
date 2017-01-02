package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality to fetch and create ProfileData objects for the AM540 protocol
 *
 * @author sva
 * @since 23/01/2015 - 14:16
 */
public class AM540LoadProfileBuilder extends LGLoadProfileBuilder {

    private IDISProfileDataReader idisProfileDataReader;

    public AM540LoadProfileBuilder(AbstractDlmsProtocol meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected boolean isIgnoreDstStatusCode() {
        return ((AM540) getMeterProtocol()).getDlmsSessionProperties().isIgnoreDstStatusCode();
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
        return getIDISProfileDataReader().fetchLoadProfileConfiguration(mbusLoadProfileReaders);
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
        return getIDISProfileDataReader().getLoadProfileData(mbusLoadProfileReaders);
    }

    private IDISProfileDataReader getIDISProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new AM540MbusProfileDataReader(getMeterProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return idisProfileDataReader;
    }
}