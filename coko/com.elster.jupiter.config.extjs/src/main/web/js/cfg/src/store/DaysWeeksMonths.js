/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.DayWeekMonth',

    data: [
        {name: 'minutes', displayValue: Uni.I18n.translate('period.minutes','CFG','minute(s)')},
        {name: 'hours', displayValue: Uni.I18n.translate('period.hours','CFG','hour(s)')},
        {name: 'days', displayValue: Uni.I18n.translate('period.days','CFG','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','CFG','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','CFG','month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years','CFG','year(s)')}
    ]
});
