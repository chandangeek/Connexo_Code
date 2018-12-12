/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.model.FirmwareFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Fwc.model.FirmwareType',
        'Fwc.model.FirmwareStatus'
    ],
    proxy: {
        type: 'querystring',
        hydrator: 'Uni.util.IdHydrator',
        root: 'filter'
    },

    associations: [
        {
            type: 'hasMany',
            model: 'Fwc.model.FirmwareType',
            name: 'firmwareType',
            associationKey: 'firmwareType'
        },
        {
            type: 'hasMany',
            model: 'Fwc.model.FirmwareStatus',
            name: 'firmwareStatus',
            associationKey: 'firmwareStatus'
        }
    ]
});