/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.model.RelativeDate', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'startAmountAgo', type: 'string', useNull: true},
        {name: 'startPeriodAgo', type: 'string', useNull: true},
        {name: 'startTimeMode', type: 'string', useNull: true},
        {name: 'startFixedDay', type: 'number', useNull: true},
        {name: 'startFixedMonth', type: 'number', useNull: true},
        {name: 'startFixedYear', type: 'number', useNull: true},
        {name: 'startNow', type: 'boolean', useNull: true},
        {name: 'onCurrentDay', type: 'boolean', useNull: true},
        {name: 'onDayOfMonth', type: 'number', useNull: true},
        {name: 'onDayOfWeek', type: 'number', useNull: true},
        {name: 'atHour', type: 'number', useNull: true},
        {name: 'atMinute', type: 'number', useNull: true}
    ]
});
