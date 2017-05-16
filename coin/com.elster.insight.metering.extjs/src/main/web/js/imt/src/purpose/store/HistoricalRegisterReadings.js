/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.HistoricalRegisterReadings', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.purpose.model.HistoricalRegisterReading'
    ],
    model: 'Imt.purpose.model.HistoricalRegisterReading',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/historicalregisterdata',
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
