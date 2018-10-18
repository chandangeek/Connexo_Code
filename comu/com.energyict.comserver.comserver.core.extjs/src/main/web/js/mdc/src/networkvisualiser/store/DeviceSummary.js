/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.networkvisualiser.store.DeviceSummary', {
    extend: 'Ext.data.Store',
    fields: [
        'id',
        'gateway',
        'name',
        'deviceType',
        'deviceConfiguration',
        'serialNumber',
        'alarms',
        'issues',
        'failedComTasks',
        'period',
        'nodeAddress',
        'shortAddress',
        'lastUpdate',
        'lastPathRequest',
        'state',
        'modulationScheme',
        'modulation',
        'linkQualityIndicator',
        'phaseInfo',
        'roundTrip',
        'linkCost'
    ],
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtg/topology/summary/{deviceName}',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        timeout: 60000,
        setUrl: function (deviceName) {
            this.url = this.urlTpl.replace('{deviceName}', deviceName);
        }
    }
});