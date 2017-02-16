/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Combine', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'localizedValue', type: 'string' },
        { name: 'breakdown', type: 'string' }
    ]
});