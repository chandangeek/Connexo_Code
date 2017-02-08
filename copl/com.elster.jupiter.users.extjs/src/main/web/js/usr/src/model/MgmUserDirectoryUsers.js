/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.MgmUserDirectoryUsers', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'ldapUsers'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.MgmUserDirectoryUser',
            associationKey: 'ldapUsers',
            name: 'ldapUsers'
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/usr/userdirectories/{userDirectoryId}/users',
        reader: {
            type: 'json',
            root: 'rules'
        },
        setUrl: function (userDirectoryId) {
            this.url = this.urlTpl.replace('{userDirectoryId}', userDirectoryId);
        }
    }
});