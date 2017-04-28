/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.HistoricalChannelReadings', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.purpose.model.HistoricalChannelReading'
    ],
    model: 'Imt.purpose.model.HistoricalChannelReading',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/historicalchanneldata',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (usagePointId, channelId, outputId) {
            this.url = this.urlTpl.replace('{usagePointId}', usagePointId).replace('{purposeId}', channelId).replace('{outputId}', outputId);
        }
    }
});