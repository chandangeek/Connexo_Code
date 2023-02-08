/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    requires: ['Fwc.firmwarecampaigns.model.DayWeekMonth'],
    model: 'Fwc.firmwarecampaigns.model.DayWeekMonth',
    data: [
        {name: 'minutes', displayValue: Uni.I18n.translate('period.minutes', 'FWC', 'minute(s)')},
        {name: 'hours', displayValue: Uni.I18n.translate('period.hours', 'FWC', 'hour(s)')},
        {name: 'days', displayValue: Uni.I18n.translate('period.days', 'FWC', 'day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks', 'FWC', 'week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months', 'FWC', 'month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years', 'FWC', 'year(s)')}
    ]
});
