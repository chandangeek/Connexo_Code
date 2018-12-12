/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.field.TimePeriod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'start', type: 'long' },
        {name: 'end', type: 'long' }
    ]
});
