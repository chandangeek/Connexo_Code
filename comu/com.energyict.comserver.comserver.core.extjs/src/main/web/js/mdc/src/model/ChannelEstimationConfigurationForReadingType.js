/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ChannelEstimationConfigurationForReadingType', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property'
    ],
    idProperty: 'ruleId',
    fields: [
        'id',
        'ruleId',
        {name: 'name', persist: false},
        {name: 'estimator', persist: false},
        'readingType',
        {name: 'isActive', persist: false}
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function () {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/estimation'
    }
});
