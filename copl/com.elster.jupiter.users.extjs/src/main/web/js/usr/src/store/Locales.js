/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.Locales', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Locale',
    proxy: {
        type: 'rest',
        url: '/api/usr/field/locales',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'locales'
        }
    }
});