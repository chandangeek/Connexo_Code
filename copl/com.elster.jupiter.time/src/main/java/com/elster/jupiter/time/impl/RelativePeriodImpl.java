package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.google.common.collect.Range;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Unique(fields="name", groups = Save.Create.class)
public class RelativePeriodImpl extends EntityImpl implements RelativePeriod {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @NotNull
    private RelativeDate from;
    @NotNull
    private RelativeDate to;

    @Inject
    public RelativePeriodImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public Range<ZonedDateTime> getInterval(ZonedDateTime referenceDate) {
        return Range.closed(this.from.getRelativeDate(referenceDate), this.to.getRelativeDate(referenceDate));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public RelativeDate getRelativeDateFrom() {
        return this.from;
    }

    @Override
    public RelativeDate getRelativeDateTo() {
        return this.to;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFrom(RelativeDate from) {
        this.from = from;
    }

    public void setTo(RelativeDate to) {
        this.to = to;
    }
}
