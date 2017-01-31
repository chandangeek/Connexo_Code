/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ldr.store.Pluggable', {
    extend: 'Ext.data.Store',
    model: 'Ldr.model.Pluggable',
    storeId: 'pluggable',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,
    remoteFilter: false,

    proxy: {
        type: 'rest',
        url: '/api/apps/pluggable',

        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json'
        }
    }
});