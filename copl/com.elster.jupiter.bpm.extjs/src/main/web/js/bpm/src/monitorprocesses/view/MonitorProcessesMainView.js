/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.MonitorProcessesMainView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-monitor-processes-main-view',
    requires: [
        'Bpm.monitorprocesses.view.RunningProcesses',
        'Bpm.monitorprocesses.view.HistoryProcesses',
        'Bpm.monitorprocesses.view.RunningProcessPreview',
        'Bpm.monitorprocesses.view.HistoryProcessPreview',
        'Bpm.monitorprocesses.view.StatusProcessPreview'
    ],
    properties: {},
    ui: 'large',
    title: Uni.I18n.translate('processes.title', 'BPM', 'Processes'),
    items: [
        {
            xtype: 'tabpanel',
            itemId: 'tab-processes',
            activeTab: -1,
            items: [
                {
                    margin: 0,
                    title: Uni.I18n.translate('processes.processesRunning.title', 'BPM', 'Running processes'),
                    itemId: 'running-processes-tab',
                    items: [
                        {
                            xtype: 'bpm-running-processes',
                            itemId: 'running-processes'
                        }
                    ]
                },
                {
                    margin: 0,
                    title: Uni.I18n.translate('processes.processesHistory.title', 'BPM', 'History'),
                    itemId: 'history-processes-tab',
                    items: [
                        {
                            xtype: 'bpm-history-processes',
                            itemId: 'history-processes'
                        }
                    ]
                }
            ]

        }
    ],
    initComponent: function () {
        var me = this;

        me.fireEvent('initStores',this.properties);
        me.callParent(arguments);

    },
    listeners: {
        'afterrender': function () {
            this.fireEvent('initComponents', this);

        }
    }
});