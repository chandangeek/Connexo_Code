Ext.define('Dxp.view.tasks.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-setup',

    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.Grid',
        'Dxp.view.tasks.Preview',
        'Dxp.view.tasks.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.dataExportTasks', 'DES', 'Data export tasks'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dxp-tasks-grid',
                        itemId: 'grd-data-export-tasks',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-export-task',
                        title: Uni.I18n.translate('dataExportTasks.empty.title', 'DES', 'No data export tasks found'),
                        reasons: [
                            Uni.I18n.translate('dataExportTasks.empty.list.item1', 'DES', 'No data export tasks have been defined yet.'),
                            Uni.I18n.translate('dataExportTasks.empty.list.item2', 'DES', 'Data export tasks exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add data export task'),
                                privileges: Dxp.privileges.DataExport.admin,
                                href: '#/administration/dataexporttasks/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'dxp-tasks-preview',
                        itemId: 'pnl-data-export-task-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});