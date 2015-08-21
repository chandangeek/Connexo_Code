package com.elster.jupiter.cbo;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.google.common.collect.ImmutableList;

import java.time.ZonedDateTime;

public class StatusTest extends EqualsContractTest {

    public static final ZonedDateTime DATE_TIME = ZonedDateTime.of(2007, 9, 18, 14, 25, 37, 123456789, TimeZoneNeutral.getMcMurdo());
    public static final String REASON = "reason";
    public static final String REMARK = "remark";
    public static final String VALUE = "value";
    private Status instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = buildA();
        }
        return instanceA;
    }

    private Status buildA() {
        return Status.builder()
                .at(DATE_TIME.toInstant())
                .reason(REASON)
                .remark(REMARK)
                .value(VALUE)
                .build();
    }

    @Override
    protected Object getInstanceEqualToA() {
        return buildA();
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                Status.builder()
                        .at(DATE_TIME.plusMinutes(3).toInstant())
                        .reason(REASON)
                        .remark(REMARK)
                        .value(VALUE)
                        .build(),
                Status.builder()
                        .at(DATE_TIME.toInstant())
                        .reason("otherReason")
                        .remark(REMARK)
                        .value(VALUE)
                        .build(),
                Status.builder()
                        .at(DATE_TIME.toInstant())
                        .reason(REASON)
                        .remark("otherRemark")
                        .value(VALUE)
                        .build(),
                Status.builder()
                        .at(DATE_TIME.toInstant())
                        .reason(REASON)
                        .remark(REMARK)
                        .value("otherValue")
                        .build()
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}