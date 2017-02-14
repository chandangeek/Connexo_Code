/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.SchedulePeriod', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'schedule',
            type: 'auto'
        },
        {
            name: 'start',
            type: 'auto'
        },
        {
            name: 'end',
            type: 'auto'
        }
    ]
});
