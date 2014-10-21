package com.elster.jupiter.time;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class RelativePeriodCategoryUsage {

    private Reference<RelativePeriod> relativePeriod = ValueReference.absent();
    private Reference<RelativePeriodCategory> relativePeriodCategory = ValueReference.absent();

    // For orm service only
    RelativePeriodCategoryUsage() {
        super();
    }

    public RelativePeriodCategoryUsage(RelativePeriod relativePeriod, RelativePeriodCategory relativePeriodCategory) {
        this();
        this.relativePeriod.set(relativePeriod);
        this.relativePeriodCategory.set(relativePeriodCategory);
    }

    public RelativePeriod getRelativePeriod() {
        return relativePeriod.get();
    }

    public RelativePeriodCategory getRelativePeriodCategory() {
        return relativePeriodCategory.get();
    }

    public boolean sameRelativePeriodCategory (RelativePeriodCategory relativePeriodCategory) {
        return this.getRelativePeriodCategory().getId() == relativePeriodCategory.getId();
    }
}
