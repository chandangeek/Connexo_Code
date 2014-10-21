package com.elster.jupiter.time;

import java.util.List;

public interface TimeService {
    static String COMPONENT_NAME = "TME";

    List<RelativePeriod> getRelativePeriods();
    RelativePeriod findRelativePeriod(long relativePeriodId);
    RelativePeriod findRelativePeriodByName(String name);
    RelativePeriod createRelativePeriod(String name, RelativeDate from, RelativeDate to);
    RelativePeriod updateRelativePeriod(Long id, String name, RelativeDate from, RelativeDate to);
    RelativePeriodCategory createRelativePeriodCategory(String key);
    RelativePeriodCategory findRelativePeriodCategory(long relativePeriodCategoryId);
    RelativePeriodCategory findRelativePeriodCategoryByName(String name);
}
