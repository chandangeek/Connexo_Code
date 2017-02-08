/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.Notifications
 */
Ext.define('Uni.store.Notifications', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Notification',
    storeId: 'notifications',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    // TODO Sort the store according on timeadded (most recent first).

    proxy: {
        type: 'memory'
    }
});