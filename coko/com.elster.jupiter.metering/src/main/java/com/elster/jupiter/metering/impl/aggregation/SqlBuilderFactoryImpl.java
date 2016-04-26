package com.elster.jupiter.metering.impl.aggregation;

import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link SqlBuilderFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (16:09)
 */
@Component(name = "com.elster.jupiter.metering.aggregation.impl.SqlBuilderFactory", service = {SqlBuilderFactory.class})
@SuppressWarnings("unused")
public class SqlBuilderFactoryImpl implements SqlBuilderFactory {
    @Override
    public ClauseAwareSqlBuilder newClauseAwareSqlBuilder() {
        return new ClauseAwareSqlBuilderImpl();
    }
}