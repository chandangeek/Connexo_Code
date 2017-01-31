/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DayWeekMonth',

    data: [
        {name: 'minutes', displayValue: Uni.I18n.translate('period.minutes','DES','minute(s)')},
        {name: 'hours', displayValue: Uni.I18n.translate('period.hours','DES','hour(s)')},
        {name: 'days', displayValue: Uni.I18n.translate('period.days','DES','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','DES','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','DES','month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years','DES','year(s)')}
    ]
});
