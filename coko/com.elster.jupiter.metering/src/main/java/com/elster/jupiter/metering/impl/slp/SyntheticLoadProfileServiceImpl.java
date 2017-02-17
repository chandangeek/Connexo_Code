/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class SyntheticLoadProfileServiceImpl implements SyntheticLoadProfileService {

    public static final long VAULT_ID = 1L;
    public static final long RECORD_SPEC_ID = 1L;

    private final DataModel dataModel;
    private final IdsService idsService;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private Vault vault;
    private RecordSpec recordSpec;

    public SyntheticLoadProfileServiceImpl(IdsService idsService, MeteringService meteringService, DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.idsService = idsService;
        this.meteringService = meteringService;
    }


    DataModel getDataModel() {
        return dataModel;
    }

    Vault getVault() {
        if (vault == null) {
            this.initVaultAndRecordSpec();
        }
        return vault;
    }

    RecordSpec getRecordSpec() {
        if (recordSpec == null) {
            this.initVaultAndRecordSpec();
        }
        return recordSpec;
    }

    @Override
    public SyntheticLoadProfileBuilder newSyntheticLoadProfile(String name, Duration interval, Period duration, Instant startTime) {
        SyntheticLoadProfileBuilderImpl syntheticLoadProfileBuilder = new SyntheticLoadProfileBuilderImpl(this, meteringService, name);
        syntheticLoadProfileBuilder.withInterval(interval);
        syntheticLoadProfileBuilder.withDuration(duration);
        syntheticLoadProfileBuilder.withStartTime(startTime);
        return syntheticLoadProfileBuilder;
    }

    @Override
    public List<SyntheticLoadProfile> findSyntheticLoadProfiles() {
        return dataModel.mapper(SyntheticLoadProfile.class).find();
    }

    @Override
    public Optional<SyntheticLoadProfile> findSyntheticLoadProfile(long id) {
        return dataModel.mapper(SyntheticLoadProfile.class).getOptional(id);
    }

    @Override
    public Optional<SyntheticLoadProfile> findSyntheticLoadProfile(String name) {
        return dataModel.mapper(SyntheticLoadProfile.class).getUnique("name", name);
    }


    private void initVaultAndRecordSpec() {
        vault = idsService.getVault(COMPONENTNAME, VAULT_ID).orElse(null);
        recordSpec = idsService.getRecordSpec(COMPONENTNAME, RECORD_SPEC_ID).orElse(null);
    }
}