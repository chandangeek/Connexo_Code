/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.store.CommandStatuses',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.commands.model.CommandStatus'
    ],
    model: 'Mdc.commands.model.CommandStatus',
    storeId: 'CommandStatuses',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/field/devicemessagestatuses',
        reader: {
            type: 'json',
            root: 'deviceMessageStatuses'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});