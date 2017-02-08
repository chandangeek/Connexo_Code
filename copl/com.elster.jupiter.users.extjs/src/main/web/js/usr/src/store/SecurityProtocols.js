/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.SecurityProtocols', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.SecurityProtocol',
    proxy: {
        type: 'memory'
    },
    data:  [
        {
            name: Uni.I18n.translate('userDirectories.securityProtocol.none', 'USR', 'None'),
            value: 'NONE'
        },
        {
            name: Uni.I18n.translate('userDirectories.securityProtocol.ssl', 'USR', 'SSL'),
            value: 'SSL'
        },
        {
            name: Uni.I18n.translate('userDirectories.securityProtocol.tls', 'USR', 'TLS'),
            value: 'TLS'
        }
    ]
});
