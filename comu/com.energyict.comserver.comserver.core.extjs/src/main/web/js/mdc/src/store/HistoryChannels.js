/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.HistoryChannels', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Mdc.model.HistoryChannel'
    ],
    model: 'Mdc.model.HistoryChannel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/channels/{channelId}/historydata',
        reader: {
            type: 'json',
            root: 'data'
        },

        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (deviceId, channelId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId).replace('{channelId}', channelId);
        }
    },

    /*
     setFilterModel: function (model) {
     var data = model.getData(),
     storeProxy = this.getProxy(),
     durationStore = Ext.getStore('Mdc.store.LoadProfileDataDurations'),
     duration = durationStore.getById(model.get('duration'));
     if (duration) {
     if (!Ext.isEmpty(data.intervalStart)) {
     storeProxy.setExtraParam('intervalStart', data.intervalStart.getTime());
     storeProxy.setExtraParam('intervalEnd', moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
     storeProxy.setExtraParam('onlySuspect', data.onlySuspect);
     storeProxy.setExtraParam('onlyNonSuspect', data.onlyNonSuspect);
     }
     }
     }
     */
});