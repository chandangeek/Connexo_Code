/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.Application', {
    extend: 'Ext.app.Application',
    name: 'CSMonitor',
    enableQuickTips: true,
    controllers: [
        'ServerDetails',
        'status.GeneralInformation',
        'status.RunningInformation',

        'status.servers.Connected',
        'status.servers.Active',
        'status.servers.Inactive',

        'status.ports.Ports',
        'status.ports.Active',
        'status.ports.Inactive',

        'status.pools.Pools',
        'status.pools.Active',
        'status.pools.Inactive',

        'performance.Storage',
        'performance.StorageChart',
        'performance.Connections',
        'performance.ThreadsChart',
        'performance.Pools',
        'performance.PoolThreadChart',
        'performance.PoolPortChart',

        'logging.Communication',
        'logging.Converter',
        'logging.DataStorage',
        'logging.general.Text',
        'logging.data.Text',
        'logging.communication.Text',

        'Converter'
    ]
});
