/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ChannelEstimationConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ChannelEstimationConfiguration'
    ],
    model: 'Mdc.model.ChannelEstimationConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/estimation',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
