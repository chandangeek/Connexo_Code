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

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('general.dataExportTasks', 'DXP', 'Data export tasks'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'tasks-menu',
                        itemId: 'tasks-view-menu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.overview', 'DXP', 'Overview'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'tasks-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('dataExportTasks.empty.title', 'DXP', 'No data export tasks found'),
                        reasons: [
                            Uni.I18n.translate('dataExportTasks.empty.list.item1', 'DXP', 'No data export tasks have been defined yet.'),
                            Uni.I18n.translate('dataExportTasks.empty.list.item2', 'DXP', 'Data export tasks exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dataExportTasks.empty.list.item3', 'DXP', 'The filter is too narrow.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addDataExportTask', 'DXP', 'Add data export task'),
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