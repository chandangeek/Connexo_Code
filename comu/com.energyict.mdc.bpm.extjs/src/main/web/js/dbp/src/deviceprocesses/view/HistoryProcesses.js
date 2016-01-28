Ext.define('Dbp.deviceprocesses.view.HistoryProcesses', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-history-processes',
    requires: [
        'Dbp.deviceprocesses.view.HistoryProcessesGrid',
        'Dbp.deviceprocesses.view.HistoryTopFilter'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemId: 'dbp-history-processes-form',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dbp-history-processes-grid',
                        itemId: 'history-processes-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'history-processes-empty-grid',
                        title: Uni.I18n.translate('dbp.process.empty.title', 'DBP', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('dbp.process.empty.historylist.item1', 'DBP', 'No processes have been defined yet.'),
                            Uni.I18n.translate('dbp.process.empty.historylist.item2', 'DBP', 'Processes exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dbp.process.empty.historylist.item3', 'DBP', 'The filter criteria are too narrow.')
                        ]
                    },
                    previewComponent: {

                        xtype: 'tabpanel',
                        deferredRender: false,
                        ui: 'large',
                        itemId: 'tab-process-preview',
                        activeTab: 0,
                        items: [
                            {
                                margin: '10 0 0 0',
                                title: Uni.I18n.translate('processes.processDetails.title', 'DBP', 'Details'),
                                itemId: 'details-process-tab',
                                items: [
                                    {
                                        xtype: 'dbp-history-process-preview',
                                        itemId: 'history-process-preview'
                                    }
                                ]
                            },
                            {
                                margin: '10 0 0 0',
                                title: Uni.I18n.translate('processes.processStatus.title', 'DBP', 'Status overview'),
                                itemId: 'status-process-tab',
                                items: [
                                    {
                                        xtype: 'dbp-status-process-preview',
                                        itemId: 'history-process-status-preview'
                                    }
                                ]
                            }
                        ]

                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'dbp-view-history-processes-topfilter',
                    itemId: 'dbp-view-history-processes-topfilter'
                }
            ]
        };
        me.callParent(arguments);
    }
});

