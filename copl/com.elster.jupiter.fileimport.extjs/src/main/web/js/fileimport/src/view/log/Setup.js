Ext.define('Fim.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-history-log-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Fim.view.log.Menu',
        'Fim.view.log.Grid',
        'Fim.view.log.Preview',
        'Fim.view.log.SortMenu'
    ],
    importService: null,
    runStartedOn: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: '',
                itemId: 'pnl-histoty-log-menu',
                ui: 'medium',
                items: [
                    {
                        xtype: 'fim-log-menu',
                        itemId: 'mnu-histoty-log',
                        toggle: 0,
                        importServiceId: me.importService.get('importServiceId')
                    }
                ]
            }
        ];
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.log', 'FIM', 'Log'),
            items: [
                {
                    xtype: 'fim-history-log-preview',
                    router: me.router,
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'filter-toolbar',
                    title: Uni.I18n.translate('importService.filter.sort', 'FIM', 'Sort'),
                    name: 'sortitemspanel',
                    itemId: 'fim-history-log-sort-toolbar',
                    emptyText: Uni.I18n.translate('general.none','FIM','None'),
                    tools: [
                        {
                            xtype: 'button',
                            action: 'addSort',
                            text: Uni.I18n.translate('general.history.addSort', 'FIM', 'Add sort'),
                            menu: {
                                xtype: 'fim-history-log-sort-menu',
                                itemId: 'menu-history-log-sort'
                            }
                        }
                    ]
                },

                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'fim-history-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('importService.log.empty.title', 'FIM', 'No logs found'),
                        reasons: [
                            me.importService.get('name') + ' ' + Uni.I18n.translate('importService.log.startedon', 'FIM', 'started on') + ' ' + me.runStartedOn + ' ' + Uni.I18n.translate('importService.log.empty.list.item1', 'FIM', 'did not create any logs.')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
