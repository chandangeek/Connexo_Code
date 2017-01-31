/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.store.Associations', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'value', type: 'string'},
        {name: 'name', type: 'string'}
    ],
    proxy: {
        type: 'memory'
    },
    data: [
        {
            name: Uni.I18n.translate('process.association.device', 'DBP', 'Device'),
            value: 'Device'
        }
    ]
});
