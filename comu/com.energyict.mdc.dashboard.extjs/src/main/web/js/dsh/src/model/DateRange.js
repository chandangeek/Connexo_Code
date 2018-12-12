/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.DateRange', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'from', type: 'date', dateFormat: 'Y-m-dTH:i:s' },
        { name: 'to', type: 'date', dateFormat: 'Y-m-dTH:i:s' }
    ]
});