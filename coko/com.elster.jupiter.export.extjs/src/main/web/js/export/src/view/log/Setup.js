Ext.define('Dxp.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.log-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dxp.view.tasks.Menu',
        'Dxp.view.log.Grid',
        'Dxp.view.log.Preview'
    ],
    task: null,
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
                        router: me.router,
                        toggle: 2
                    }
                ]
            }
        ];
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.log', 'DES', 'Log'),
            items: [
                {
                    xtype: 'log-preview',
                    router: me.router,
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'log-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('log.empty.title', 'DES', 'No logging has been found.'),
                        reasons: [
                            Uni.I18n.translate('log.empty.list.item1', 'DES', 'No logging has been found for the') + ' ' + me.task.get('name') + ' ' + Uni.I18n.translate('general.startedon', 'DES', 'started on') + ' ' + me.task.get('name')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
