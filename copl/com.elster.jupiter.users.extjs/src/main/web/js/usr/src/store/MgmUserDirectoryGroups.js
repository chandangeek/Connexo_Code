/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.MgmUserDirectoryGroups', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.MgmUserDirectoryGroup',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/usr/userdirectories/{userDirectoryId}/extimportedgroups',
        timeout: 240000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'extimportedgroups'
        },
        setUrl: function (userDirectoryId) {
            this.url = this.urlTpl.replace('{userDirectoryId}', userDirectoryId);
        }
    }
});
