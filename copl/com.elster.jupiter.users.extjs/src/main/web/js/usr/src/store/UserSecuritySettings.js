/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.UserSecuritySettings', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.UserSecuritySettings',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/usr/userSecuritySettings',
        timeout: 240000,
        reader: {
            type: 'json',
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});