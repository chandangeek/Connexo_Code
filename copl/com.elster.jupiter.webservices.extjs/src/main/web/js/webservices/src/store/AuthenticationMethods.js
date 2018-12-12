/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.AuthenticationMethods', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.AuthenticationMethod',
    proxy: {
        type: 'rest',
        url: '/api/ws/fields/authenticationMethod',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'authenticationMethods'
        }
    }
});