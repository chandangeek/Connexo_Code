/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.MgmUserDirectoryUsers', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.MgmUserDirectoryUser',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/usr/userdirectories/{userDirectoryId}/users',
        timeout: 240000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'users'
        },
        setUrl: function (userDirectoryId) {
            this.url = this.urlTpl.replace('{userDirectoryId}', userDirectoryId);
        }
    }
});
