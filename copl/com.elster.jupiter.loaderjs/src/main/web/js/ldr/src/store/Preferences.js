/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ldr.store.Preferences
 */
Ext.define('Ldr.store.Preferences', {
    extend: 'Ext.data.Store',
    model: 'Ldr.model.Preference',
    storeId: 'preferences',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,
    remoteFilter: false,

    proxy: {
        type: 'rest',
        url: '/api/usr/currentuser/preferences',

        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json',
            root: 'preferences'
        }
    }
});