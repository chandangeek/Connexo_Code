/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.EventTypes', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.EventType',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/evt/eventtypes',
        reader: {
            type: 'json',
            root: 'eventTypes'
        }
    }
});

