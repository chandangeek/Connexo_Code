/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.MgmUserDirectoryUser', {
    extend: 'Ext.data.Model',
    fields: [
       'id',
        'name',
        'status',
        {
            name: 'statusDisplay',
            persist: false,
            convert: function (value, record) {
                return record.get('status')? Uni.I18n.translate('userDirectories.userStatus.active', 'USR', 'Active') :
                    Uni.I18n.translate('userDirectories.userStatus.inactive', 'USR', 'Inactive');
            }
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