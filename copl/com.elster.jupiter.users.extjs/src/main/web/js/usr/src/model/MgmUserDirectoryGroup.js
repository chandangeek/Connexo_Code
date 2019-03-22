/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.MgmUserDirectoryGroup', {
    extend: 'Ext.data.Model',
    fields: [
       'id',
       'name'
    ],
    
    proxy: {
        type: 'rest',
        url: '/api/usr/userdirectories/groups',
        reader: {
            type: 'json',
            root: 'rules'
        }
    }
});