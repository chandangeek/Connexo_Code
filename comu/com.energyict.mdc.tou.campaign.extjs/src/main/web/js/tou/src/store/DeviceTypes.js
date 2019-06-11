/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.DeviceTypes', {
    extend: 'Ext.data.Store',
    model: 'Tou.model.DeviceType',

    proxy: {
        type: 'rest',
        url: '/api/tou/toucampaigns/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});