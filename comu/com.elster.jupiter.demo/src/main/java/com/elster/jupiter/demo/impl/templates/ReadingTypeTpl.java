package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ReadingTypeBuilder;
import com.elster.jupiter.metering.ReadingType;

public enum ReadingTypeTpl implements Template<ReadingType, ReadingTypeBuilder> {
    _0_0_2_1_2_1_12_0_0_0_0_0_0_0_0_0_73_0 ("0.0.2.1.2.1.12.0.0.0.0.0.0.0.0.0.73.0", "Bulk Reactive Energy + all phases"),
    _0_0_2_1_2_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.2.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy + all phases"),
    _0_0_2_4_2_1_12_0_0_0_0_0_0_0_0_0_73_0 ("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.0.73.0", "Delta Reactive Energy + all phases"),
    _0_0_2_4_2_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy + all phases"),
    _0_0_2_4_2_1_12_0_0_0_0_0_0_0_0_6_73_0 ("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.6.73.0", "Delta Reactive Energy + all phases"),
    _0_0_2_1_2_1_12_0_0_0_0_0_0_0_0_6_73_0 ("0.0.2.1.2.1.12.0.0.0.0.0.0.0.0.6.73.0", "Bulk Reactive Energy + all phases"),
    _0_0_2_1_3_1_12_0_0_0_0_0_0_0_0_0_73_0 ("0.0.2.1.3.1.12.0.0.0.0.0.0.0.0.0.73.0", "Bulk Reactive Energy - all phases"),
    _0_0_2_1_3_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.2.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy - all phases"),
    _0_0_2_1_3_1_12_0_0_0_0_0_0_0_0_6_73_0 ("0.0.2.1.3.1.12.0.0.0.0.0.0.0.0.6.73.0", "Bulk Reactive Energy - all phases"),
    _0_0_2_4_3_1_12_0_0_0_0_0_0_0_0_0_73_0 ("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.0.73.0", "Delta Reactive Energy - all phases"),
    _0_0_2_4_3_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy - all phases"),
    _0_0_2_4_3_1_12_0_0_0_0_0_0_0_0_6_73_0 ("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.6.73.0", "Delta Reactive Energy - all phases"),
    _0_0_0_1_2_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.0.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy + all phases"),
    _0_0_0_4_2_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.0.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy + all phases"),
    _0_0_0_4_3_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.0.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy - all phases"),
    _0_0_0_1_3_1_12_0_0_0_0_0_0_0_0_3_73_0 ("0.0.0.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy - all phases"),
    ;

    private String mrid;
    private String alias;

    ReadingTypeTpl(String mrid, String alias) {
        this.mrid = mrid;
        this.alias = alias;
    }

    @Override
    public Class<ReadingTypeBuilder> getBuilderClass() {
        return ReadingTypeBuilder.class;
    }

    @Override
    public ReadingTypeBuilder get(ReadingTypeBuilder builder) {
        return builder.withMrid(mrid).withAlias(alias);
    }
}
