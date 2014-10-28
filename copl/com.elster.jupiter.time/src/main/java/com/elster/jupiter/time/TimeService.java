package com.elster.jupiter.time;

import java.util.List;
import java.util.Optional;

public interface TimeService {
    static String COMPONENT_NAME = "TME";

    List<RelativePeriod> getRelativePeriods();
    Optional<RelativePeriod> findRelativePeriod(long relativePeriodId);
    Optional<RelativePeriod> findRelativePeriodByName(String name);
    RelativePeriod createRelativePeriod(String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories);
    RelativePeriod updateRelativePeriod(Long id, String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories);
    RelativePeriodCategory createRelativePeriodCategory(String key);
    Optional<RelativePeriodCategory> findRelativePeriodCategory(long relativePeriodCategoryId);
    Optional<RelativePeriodCategory> findRelativePeriodCategoryByName(String name);
    List<RelativePeriodCategory> getRelativePeriodCategories();
}
