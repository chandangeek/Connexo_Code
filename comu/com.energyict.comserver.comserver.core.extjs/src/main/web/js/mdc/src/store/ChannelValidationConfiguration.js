/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ChannelValidationConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ChannelValidationConfiguration'
    ],
    model: 'Mdc.model.ChannelValidationConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/validation',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
