/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.DayWeekMonth'],
    model: 'Est.estimationtasks.model.DayWeekMonth',
    data: [
        {name: 'minutes', displayValue: Uni.I18n.translate('period.minutes','EST','minute(s)')},
        {name: 'hours', displayValue: Uni.I18n.translate('period.hours','EST','hour(s)')},
        {name: 'days', displayValue: Uni.I18n.translate('period.days','EST','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','EST','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','EST','month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years','EST','year(s)')}
    ]
});
