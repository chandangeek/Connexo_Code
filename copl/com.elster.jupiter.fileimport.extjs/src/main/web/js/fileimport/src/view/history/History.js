Ext.define('Fim.view.history.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-import-service-history',
    requires: [
        'Fim.view.history.HistoryPreview',
        'Fim.view.history.HistoryFilterForm',
        'Fim.view.history.HistoryGrid',
        'Fim.view.history.Menu',
        'Uni.component.filter.view.FilterTopPanel',
        'Fim.view.history.HistoryPreviewForm',
        'Uni.component.filter.view.FilterTopPanel',
        'Fim.view.history.SortMenu'
    ],

    router: null,
    importServiceId: null,
    showImportService: false,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'fim-history-menu',
                        itemId: 'history-view-menu',
                        importServiceId: me.importServiceId,
                        showImportService: me.showImportService,
                        router: me.router
                    },
                    {
                        xtype: 'fim-history-filter-form',
                        itemId: 'history-filter-form',
                        router: me.router,
                        showImportService: me.showImportService
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'FIM', 'History'),
            items: [
                {
                    xtype: 'filter-top-panel',
                    itemId: 'flt-import-service-history-top-panel',
                    emptyText: Uni.I18n.translate('general.none', 'FIM', 'None')

                },
                {xtype: 'menuseparator'},
                {
                    xtype: 'filter-toolbar',
                    title: Uni.I18n.translate('importService.filter.sort', 'MDC', 'Sort'),
                    name: 'sortitemspanel',
                    itemId: 'fim-history-sort-toolbar',
                    emptyText: 'None',
                    tools: [
                        {
                            xtype: 'button',
                            action: 'addSort',
                            text: Uni.I18n.translate('general.history.addSort', 'FIM', 'Add sort'),
                            menu: {
                                xtype: 'fim-history-sort-menu',
                                itemId: 'menu-history-sort',
                                name: 'addsortitemmenu'
                            }
                        }
                    ]
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'fim-history-grid',
                        router: me.router,
                        showImportService: me.showImportService
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('importService.history.empty.title', 'FIM', 'No import service history found'),
                        reasons: [
                            Uni.I18n.translate('importService.history.empty.list.item1', 'FIM', 'There is no history available for this import service.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'fim-history-preview',
                        itemId: 'pnl-history-preview'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

