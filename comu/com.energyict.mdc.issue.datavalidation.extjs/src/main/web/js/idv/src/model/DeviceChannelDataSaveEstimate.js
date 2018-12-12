/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idv.model.DeviceChannelDataSaveEstimate', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        'estimatorImpl',
        'estimateBulk',
        'intervals',
        'readingType'
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/data/issue/estimate',
        reader: {
            type: 'json'
        }
    }
});

