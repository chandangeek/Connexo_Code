/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.DayWeekMonth',

    data: [
        {name: 'minutes', displayValue: Uni.I18n.translate('period.minutes', 'APR', 'minute(s)')},
        {name: 'hours', displayValue: Uni.I18n.translate('period.hours', 'APR', 'hour(s)')},
        {name: 'days', displayValue: Uni.I18n.translate('period.days', 'APR', 'day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks', 'APR', 'week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months', 'APR', 'month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years', 'APR', 'year(s)')}
    ]
});
