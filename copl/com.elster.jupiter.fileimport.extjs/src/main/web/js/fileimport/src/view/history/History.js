Ext.define('Fim.view.history.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-import-service-history',
    requires: [
        'Fim.view.history.HistoryPreview',
        'Fim.view.history.ImportServicesHistoryTopFilter',
        'Fim.view.history.HistoryGrid',
        'Fim.view.history.Menu',
        'Fim.view.history.HistoryPreviewForm',
        'Fim.view.history.SortMenu'
    ],

    router: null,
    importServiceId: null,
    showImportService: false,
    initComponent: function () {
        var me = this;

        if (!me.showImportService) {
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
                        }
                    ]
                }
            ];
        }

        me.content = {
            xtype: 'panel',
            itemid : 'fim-history-form',
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'FIM', 'History'),
            items: [
                {
                    xtype: 'filter-toolbar',
                    title: Uni.I18n.translate('importService.filter.sort', 'FIM', 'Sort'),
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
                        itemId: 'fim-history-grid',
                        router: me.router,
                        showImportService: me.showImportService
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'fim-history-empty-grid',
                        title: Uni.I18n.translate('importService.history.empty.title', 'FIM', 'No import service history found'),
                        reasons: [
                            Uni.I18n.translate('importService.history.empty.list.item1', 'FIM', 'There is no history available for this import service.'),
                            Uni.I18n.translate('importService.history.empty.list.item2', 'FIM', 'The filter criteria are too narrow.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'fim-history-preview',
                        itemId: 'pnl-history-preview'
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'fim-view-history-importservices-topfilter',
                    itemId: 'fim-view-history-topfilter',
                    includeServiceCombo: me.showImportService
                }
            ]
        };
        me.callParent(arguments);
    }
});

