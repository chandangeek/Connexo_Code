/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.PayloadSaveStrategy', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.PayloadSaveStrategy',
    proxy: {
        type: 'rest',
        url: '/api/ws/fields/payloadstrategies',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'payloadStrategies'
        }
    }
});