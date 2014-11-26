Ext.define('Dxp.view.datasources.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-sources-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dxp.view.tasks.Menu',
        'Dxp.view.datasources.Grid'
    ],
    taskId: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('general.dataExportTask', 'DES', 'Data export task'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'tasks-menu',
                        itemId: 'tasks-view-menu',
                        taskId: me.taskId,
                        router: me.router,
                        toggle: 2
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'data-sources-grid',
                        taskId: me.taskId
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('dataSources.empty.title', 'DES', 'No data sources found'),
                        reasons: [
                            Uni.I18n.translate('dataSources.empty.list.item1', 'DES', 'No data sources have been defined yet.'),
                            Uni.I18n.translate('dataSources.empty.list.item2', 'DES', 'Data sources exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dataExportTasks.empty.list.item3', 'DES', 'The filter is too narrow.')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

