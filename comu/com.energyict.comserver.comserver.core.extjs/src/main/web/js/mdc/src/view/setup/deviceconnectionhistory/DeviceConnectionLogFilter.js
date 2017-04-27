/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-deviceconnectionhistory-connectionlogfilter',

    store: 'DeviceConnectionLog',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'logLevels',
            emptyText: Uni.I18n.translate('deviceconnectionhistory.logLevel', 'MDC', 'Log level'),
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.debug', 'MDC', 'Debug'),
                    value: 'Debug'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.trace', 'MDC', 'Trace'),
                    value: 'Trace'
                }
            ]
        },
        {
            type: 'combobox',
            dataIndex: 'logTypes',
            emptyText: Uni.I18n.translate('deviceconnectionhistory.logType', 'MDC', 'Log type'),
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.connection', 'MDC', 'Connections'),
                    value: 'Connections'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.communicationTasks', 'MDC', 'Communication tasks'),
                    value: 'Communications'
                }
            ]
        }
    ]
});