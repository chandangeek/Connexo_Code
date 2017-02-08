/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.MgmUserDirectory', {
    extend: 'Ext.data.Model',
    fields: [
       'id',
        'name',
        'url',
        'isDefault',
        'securityProtocol',
        {
            name: 'securityProtocolDisplay',
            persist: false,
            convert: function (value, record) {
                switch(record.get('securityProtocol')) {
                    case 'NONE':
                        return Uni.I18n.translate('userDirectories.securityProtocol.none', 'USR', 'None');
                        break;
                    case 'SSL':
                        return Uni.I18n.translate('userDirectories.securityProtocol.ssl', 'USR', 'SSL');
                        break;
                    case 'TLS':
                        return Uni.I18n.translate('userDirectories.securityProtocol.tls', 'USR', 'TLS');
                        break;
                    default:
                        return record.get('securityProtocol');
                }
            }
        },
        'type',
        {
            name: 'typeDisplay',
            persist: false,
            convert: function (value, record) {
                if (record.get('id') === 0) {
                    return Uni.I18n.translate('userDirectories.type.internal', 'USR', 'Internal');
                }
                switch(record.get('type')) {
                    case 'ACD':
                        return Uni.I18n.translate('userDirectories.type.activeDirectory', 'USR', 'Active Directory');
                        break;
                    case 'APD':
                        return Uni.I18n.translate('userDirectories.type.apacheDS', 'USR', 'LDAP');
                        break;
                    default:
                        return Uni.I18n.translate('userDirectories.type.none', 'USR', 'None');
                }
            }
        },
        'directoryUser',
        'password',
        'backupUrl',
        'baseUser',
        'baseGroup'
    ],
    
    proxy: {
        type: 'rest',
        url: '/api/usr/userdirectories',
        reader: {
            type: 'json'
        }
    }
});