/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskLogFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicecommunicationtaskhistory-communicationlogfilter',

    store: 'DeviceCommunicationTaskLog',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'logLevels',
            emptyText: Uni.I18n.translate('devicecommunicationtaskhistory.logLevel', 'MDC', 'Log level'),
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.debug', 'MDC', 'Debug'),
                    value: 'Debug'
                },
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.trace', 'MDC', 'Trace'),
                    value: 'Trace'
                }
            ]
        }
    ]
});