/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.Roles', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.Role',
    proxy: {
        type: 'rest',
        url: '/api/ws/fields/roles',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'roles'
        }
    }
});