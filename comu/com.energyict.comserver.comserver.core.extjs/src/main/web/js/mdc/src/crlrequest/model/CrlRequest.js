/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.model.CrlRequest', {
    extend: 'Uni.model.Version',
    requires: [
    ],
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'securityAccessor', type: 'auto', defaultValue: null},
        {name: 'certificateAlias', type: 'auto', defaultValue: null},
        {name: 'caName', type: 'auto', defaultValue: null},
        {name: 'requestFrequency', type: 'auto', defaultValue: null},
        {
            name: 'securityAccessor',
            persist: false,
            convert: function (value, record) {
                if (record.get('certificateAlias')) {
                    return Uni.I18n.translate('general.certificates', 'MDC', 'Certificates');
                } else if (record.get('keyAlias')) {
                    return Uni.I18n.translate('general.keys', 'MDC', 'Keys');
                }
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/crls'
    }
});