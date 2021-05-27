/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.User', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'authenticationName',
        'description',
        'version',
        'domain',
        'email',
        'active',
        {
            name: 'statusDisplay',
            persist: false,
            mapping: function (data) {
                if(data.active){
                    return Uni.I18n.translate('general.active', 'USR', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'USR', 'Inactive');
                }
            }
        },
        {name: 'language', type: 'auto', defaultValue: {}},
        'createdOn',
        'modifiedOn',
        'lastSuccessfulLogin',
        'lastUnSuccessfulLogin',
        'isUserLocked',
        {
            name: 'accountLocked',
            persist: false,
            mapping: function (data) {
                if(data.isUserLocked){
                    return Uni.I18n.translate('general.userLocked', 'USR', 'Yes');
                } else {
                    return Uni.I18n.translate('general.userUnlocked', 'USR', 'No');
                }
            }
        },
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.Group',
            associationKey: 'groups',
            name: 'groups'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json'
        }
    }
});