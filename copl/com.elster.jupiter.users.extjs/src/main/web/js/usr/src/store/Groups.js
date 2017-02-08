/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.Groups', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Group',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/usr/groups',
        reader: {
            type: 'json',
            root: 'groups'
        }
    }
});