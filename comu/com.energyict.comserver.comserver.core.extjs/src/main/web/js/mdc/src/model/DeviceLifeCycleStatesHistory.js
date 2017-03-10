/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceLifeCycleStatesHistory', {
    extend: 'Ext.data.Model',
    fields: [
        'from',
        'to',
        'author',
        'modTime',
        'type'
    ]
});
