/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.store.Commands', {
    extend: 'Ext.data.Store',
    model: 'Mdc.commands.model.Command',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicemessages',
        timeout: 9999999,
        reader: {
            type: 'json',
            root: 'deviceMessages'
        }
    }
});