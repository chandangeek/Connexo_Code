Ext.define('Dxp.view.tasks.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-history',
    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.PreviewForm',
        'Dxp.view.tasks.HistoryGrid'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('general.dataExportTasks', 'DES', 'Data export tasks'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'tasks-menu',
                        itemId: 'tasks-view-menu',
                        router: me.router,
                        toggle: 1
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'DES', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'tasks-history-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('dataExportTasks.empty.title', 'DES', 'No data export tasks found'),
                        reasons: [
                            Uni.I18n.translate('dataExportTasks.empty.list.item1', 'DES', 'No data export tasks have been defined yet.'),
                            Uni.I18n.translate('dataExportTasks.empty.list.item2', 'DES', 'Data export tasks exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dataExportTasks.empty.list.item3', 'DES', 'The filter is too narrow.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add data export task'),
                                ui: 'action',
                                href: '#/administration/dataexporttasks/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'tasks-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

