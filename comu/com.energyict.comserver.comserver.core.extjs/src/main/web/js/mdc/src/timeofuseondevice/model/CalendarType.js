/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.model.CalendarType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'localizedValue', type: 'string'},
        {name: 'calendarType', type: 'number'}
    ]
});
