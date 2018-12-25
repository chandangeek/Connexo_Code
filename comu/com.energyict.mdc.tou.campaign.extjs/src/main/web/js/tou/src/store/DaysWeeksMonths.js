/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    requires: ['Tou.model.DayWeekMonth'],
    model: 'Tou.model.DayWeekMonth',
    data: [
        {name: 'minutes', displayValue: 'minute(s)'},
        {name: 'hours', displayValue: 'hour(s)'},
        {name: 'days', displayValue: 'day(s)'},
        {name: 'weeks', displayValue: 'week(s)'},
    ]
});