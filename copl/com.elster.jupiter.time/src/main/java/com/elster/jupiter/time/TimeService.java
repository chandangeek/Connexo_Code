package com.elster.jupiter.time;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.cron.CronExpression;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@ProviderType
public interface TimeService {
    static String COMPONENT_NAME = "TME";

    List<RelativePeriod> getRelativePeriods();
    Optional<RelativePeriod> findRelativePeriod(long relativePeriodId);
    Optional<RelativePeriod> findAndLockRelativePeriodByIdAndVersion(long id, long version);
    Optional<RelativePeriod> findRelativePeriodByName(String name);
    RelativePeriod createRelativePeriod(String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories);
    RelativePeriod updateRelativePeriod(Long id, String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories);
    RelativePeriodCategory createRelativePeriodCategory(String key);
    Optional<RelativePeriodCategory> findRelativePeriodCategory(long relativePeriodCategoryId);
    Optional<RelativePeriodCategory> findRelativePeriodCategoryByName(String name);
    List<RelativePeriodCategory> getRelativePeriodCategories();

    Query<? extends RelativePeriod> getRelativePeriodQuery();

    String toLocalizedString(PeriodicalScheduleExpression expression);
    String toLocalizedString(CronExpression expression, Locale locale);

    RelativePeriod getAllRelativePeriod();
}
