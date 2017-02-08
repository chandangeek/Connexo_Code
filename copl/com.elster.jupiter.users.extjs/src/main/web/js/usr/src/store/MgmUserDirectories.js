/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.MgmUserDirectories', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.MgmUserDirectory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/usr/userdirectories',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'userDirectories'
        }
    }

});
