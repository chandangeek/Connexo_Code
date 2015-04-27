Ext.define('Dxp.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.log-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dxp.view.log.Menu',
        'Dxp.view.log.Grid',
        'Dxp.view.log.Preview'
    ],
    task: null,
    runStartedOn: null,
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
                        xtype: 'dxp-log-menu',
                        itemId: 'log-view-menu',
                        toggle: 0
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
                    xtype: 'dxp-log-preview',
                    router: me.router,
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dxp-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('log.empty.title', 'DES', 'No logs found'),
                        reasons: [
                            me.task.get('name') + ' ' + Uni.I18n.translate('general.startedon', 'DES', 'started on') + ' ' + me.runStartedOn + ' ' + Uni.I18n.translate('log.empty.list.item1', 'DES', 'did not create any logs.')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
