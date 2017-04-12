/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.readings.ReadingCorrection', {
    extend: 'Ext.data.Model',
    requires: [
    ],

    fields: [
        'type',
        'onlySuspectOrEstimated',
        'projected',
        'estimationComment',
        'amount',
        'intervals'
    ],

    proxy: {
        type: 'rest',
        mdmUrl: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/channelData/correctValues',
        mdcUrl: '/api/ddr/devices/{deviceId}/channels/{channelId}/data/correctValues',
        reader: {
            type: 'json'
        },
        setMdmUrl: function(usagePointId, purposeId, outputId){
            this.url = this.mdmUrl.replace('{usagePointId}', usagePointId).replace('{purposeId}', purposeId).replace('{outputId}', outputId)
        },
        setMdcUrl: function(deviceId, channelId){
            this.url = this.mdcUrl.replace('{deviceId}', deviceId).replace('{channelId}', channelId)
        }
    }
});
