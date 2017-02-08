/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.MgmUserDirectoryExtUsers', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.MgmUserDirectoryUser',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/usr/userdirectories/{userDirectoryId}/extusers',
        timeout: 240000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'extusers'
        },
        setUrl: function (userDirectoryId) {
            this.url = this.urlTpl.replace('{userDirectoryId}', userDirectoryId);
        }
    }
});
