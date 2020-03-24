/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisteredNotificationEndpoints', {
    extend: 'Ext.data.Store',
    requires: ['Mdc.model.RegisteredNotificationEndpoints'],
    model: 'Mdc.model.RegisteredNotificationEndpoints',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/sap/registerednotificationendpoints',
        reader: {
            type: 'json',
            root: 'registeredNotificationEndpoints'
        },
    }
});