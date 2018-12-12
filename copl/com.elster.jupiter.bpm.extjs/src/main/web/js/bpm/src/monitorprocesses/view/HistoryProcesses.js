/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.HistoryProcesses', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-history-processes',
    requires: [
        'Bpm.monitorprocesses.view.HistoryProcessesGrid',
        'Bpm.monitorprocesses.view.HistoryTopFilter'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            grid: {
                xtype: 'bpm-history-processes-grid',
                itemId: 'history-processes-grid',
                router: me.router
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'history-processes-empty-grid',
                title: Uni.I18n.translate('bpm.process.empty.title', 'BPM', 'No processes found'),
                reasons: [
                    Uni.I18n.translate('bpm.process.empty.historylist.item1', 'BPM', 'No processes have been defined yet.'),
                    Uni.I18n.translate('bpm.process.empty.historylist.item2', 'BPM', 'Processes exist, but you do not have permission to view them.'),
                    Uni.I18n.translate('bpm.process.empty.historylist.item3', 'BPM', 'No processes comply with the filter.')
                ]
            },
            previewComponent: {
                xtype: 'tabpanel',
                deferredRender: false,
                ui: 'large',
                itemId: 'tab-history-process-preview',
                activeTab: 0,
                items: [
                    {
                        margin: '10 0 0 0',
                        title: Uni.I18n.translate('processes.processDetails.title', 'BPM', 'Details'),
                        itemId: 'details-process-tab',
                        items: [
                            {
                                xtype: 'bpm-history-process-preview',
                                itemId: 'history-process-preview'
                            }
                        ]
                    },
                    {
                        margin: '10 0 0 0',
                        title: Uni.I18n.translate('processes.processStatus.title', 'BPM', 'Status overview'),
                        itemId: 'status-process-tab',
                        items: [
                            {
                                xtype: 'bpm-status-process-preview',
                                itemId: 'history-process-status-preview'
                            }
                        ]
                    }
                ]
            }
        };

        me.dockedItems = [{
            dock: 'top',
            xtype: 'bpm-view-history-processes-topfilter',
            itemId: 'bpm-view-history-processes-topfilter'
        }];

        me.callParent(arguments);
    }
});

