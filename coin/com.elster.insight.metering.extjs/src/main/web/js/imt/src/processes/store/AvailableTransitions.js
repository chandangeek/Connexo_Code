/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.store.AvailableTransitions', {
    extend: 'Ext.data.Store',
    model: 'Imt.processes.model.Transition',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/transitions/{toStage}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'transitions'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});