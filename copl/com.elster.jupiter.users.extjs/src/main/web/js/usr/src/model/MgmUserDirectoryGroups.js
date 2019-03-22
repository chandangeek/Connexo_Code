/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.MgmUserDirectoryGroups', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'ldapGroups'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.MgmUserDirectoryGroup',
            associationKey: 'ldapGroups',
            name: 'ldapGroups'
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/usr/userdirectories/groups',
        reader: {
            type: 'json',
            root: 'rules'
        }/*,
        setUrl: function (userDirectoryId) {
            this.url = this.urlTpl.replace('{userDirectoryId}', userDirectoryId);
        }*/
    }
});