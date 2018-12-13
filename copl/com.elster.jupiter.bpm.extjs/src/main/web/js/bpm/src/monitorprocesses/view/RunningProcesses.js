/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.RunningProcesses', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-running-processes',
    requires: [
        'Bpm.monitorprocesses.view.RunningProcessesGrid'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'bpm-running-processes-grid',
                    itemId: 'running-processes-grid',
                    router: me.router
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'running-processes-empty-grid',
                    title: Uni.I18n.translate('bpm.process.empty.title', 'BPM', 'No processes found'),
                    reasons: [
                        Uni.I18n.translate('bpm.process.empty.runnninglist.item1', 'BPM', 'No processes have been defined yet.'),
                        Uni.I18n.translate('bpm.process.empty.runnninglist.item2', 'BPM', 'Processes exist, but you do not have permission to view them.')
                    ]
                },
                previewComponent: {
                    xtype: 'tabpanel',
                    deferredRender : false,
                    ui: 'large',
                    itemId: 'tab-running-process-preview',
                    activeTab: 0,
                    items: [
                        {
                            margin: '10 0 0 0',
                            title: Uni.I18n.translate('processes.processDetails.title', 'BPM', 'Details'),
                            itemId: 'details-process-tab',
                            items: [
                                {
                                    xtype: 'bpm-running-process-preview',
                                    itemId: 'running-process-preview'
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
                                    itemId: 'running-process-status-preview'
                                }
                            ]
                        }
                    ]

                }
            }
        ];
        me.callParent(arguments);
    }
});

