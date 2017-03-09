/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class SyntheticLoadProfileServiceImpl implements SyntheticLoadProfileService {

    public static final long VAULT_ID = 1L;
    public static final long RECORD_SPEC_ID = 1L;

    private final DataModel dataModel;
    private final IdsService idsService;
    private Vault vault;
    private RecordSpec recordSpec;

    public SyntheticLoadProfileServiceImpl(IdsService idsService, DataModel dataModel) {
        this.dataModel = dataModel;
        this.idsService = idsService;
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
    public SyntheticLoadProfileBuilder newSyntheticLoadProfile(String name, Period duration, Instant startTime, ReadingType readingType) {
        SyntheticLoadProfileBuilderImpl syntheticLoadProfileBuilder = new SyntheticLoadProfileBuilderImpl(this, name);
        syntheticLoadProfileBuilder.withDuration(duration);
        syntheticLoadProfileBuilder.withStartTime(startTime);
        syntheticLoadProfileBuilder.withReadingType(readingType);
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