/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-history-log-setup',
    requires: [
        'Uni.util.FormEmptyMessage',
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
                itemId: 'pnl-histoty-log-menu',
                ui: 'medium',
                items: [
                    {
                        xtype: 'fim-log-menu',
                        itemId: 'mnu-histoty-log',
                        router: me.router,
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
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('importService.log.startedOnNoLogs', 'FIM', '{0} started on {1} did not create any logs',[me.importService.get('name'),me.runStartedOn])
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
