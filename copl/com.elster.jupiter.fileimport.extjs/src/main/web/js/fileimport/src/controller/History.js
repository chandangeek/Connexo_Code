Ext.define('Fim.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Fim.privileges.DataImport'
    ],
    views: [
        'Fim.view.history.History'
    ],
    stores: [
        'Fim.store.Status',
		'Fim.store.ImportServicesHistory',
        'Fim.store.ImportServicesFilter'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'fim-import-service-history'
        },
        {
            ref: 'historyViewMenu',
            selector: '#history-view-menu'
        },
        {
            ref: 'historyGrid',
            selector: 'fim-import-service-history fim-history-grid'
        },
        {
            ref: 'historySortToolbar',
            selector: '#fim-history-sort-toolbar'
        }
    ],
    applicationName: null,

    init: function () {
        this.control({
            'fim-import-service-history fim-history-grid': {
                select: this.showPreview
            },
            'fim-history-sort-menu': {
                click: this.chooseSort
            },
            '#fim-history-sort-toolbar button[action=clear]': {
                click: this.clearAllSorting
            },
            '#fim-history-sort-toolbar button': {
                closeclick: this.onSortCloseClicked
            },
            '#fim-history-sort-toolbar #itemsContainer button': {
                click: this.switchSortingOrder
            },
            'fim-history-action-menu': {
                click: this.chooseAction
            }
        });

        applicationName = typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : '';
    },

    showImportServicesHistory: function (importServiceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Fim.store.ImportServicesHistory'),
            importServiceModel = me.getModel('Fim.model.ImportServiceDetails'),
            noSpecificImportService = (importServiceId === undefined),
            view,
            proxyArgument = {};

        proxyArgument.importServiceId = importServiceId;
        store.getProxy().setUrl(proxyArgument);
        me.setDefaultSort();

        view = Ext.widget('fim-import-service-history', {
            router: router,
            importServiceId: importServiceId,
            showImportService: noSpecificImportService
        });

        me.getApplication().fireEvent('changecontentevent', view);
        if (!noSpecificImportService) {
            me.getHistoryViewMenu().setTitle(Uni.I18n.translate('general.importService', 'FIM', 'Import services'));
        }

        me.updateSortingToolbar();

        if (!noSpecificImportService) {
            importServiceModel.load(importServiceId, {
                success: function (record) {
                    view.down('#history-view-menu #import-service-view-link').setText(record.get('name'));
                    me.getApplication().fireEvent('importserviceload', record);
                }
            });
        }
    },

    updateSortingToolbar: function () {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            store = me.getStore('Fim.store.ImportServicesHistory');

        sortContainer.removeAll();
        sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    menuItem = me.getPage().down('#menu-history-sort [name=' + sortItem.property + ']');
                    cls = sortItem.direction === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'history-sort-by-' + sortItem.property + '-button',
                        text: menuItem.text,
                        sortType: sortItem.property,
                        sortDirection: sortItem.direction,
                        iconCls: cls
                    });
                }
            });
        }
    },

    setDefaultSort: function () {
        var me = this,
            store = me.getStore('Fim.store.ImportServicesHistory'),
            sorting = store.getProxy().extraParams['sort'];

        if (sorting === undefined) { // set default filters
            sorting = [];
            sorting.push({
                property: 'status',
                direction: Uni.component.sort.model.Sort.DESC
            });
            sorting.push({
                property: 'startDate',
                direction: Uni.component.sort.model.Sort.DESC
            });
            store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('fim-history-preview'),
            previewForm = page.down('fim-history-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('startedOnDisplay'));
        previewForm.loadRecord(record);
        preview.down('fim-history-action-menu').record = record;
        Ext.resumeLayouts();
    },

    chooseSort: function (menu, item) {
        var me = this,
            name = item.name,
            store = me.getStore('Fim.store.ImportServicesHistory'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.property === name
            });

            if (sortingItem) {
                return;
            } else {
                sorting.push({
                    property: name,
                    direction: Uni.component.sort.model.Sort.DESC
                });
            }
        } else {
            sorting = [
                {
                    property: name,
                    direction: Uni.component.sort.model.Sort.DESC
                }
            ];
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    clearAllSorting: function (btn) {
        var me = this,
            store = me.getStore('Fim.store.ImportServicesHistory');

        sorting = [];
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    onSortCloseClicked: function (btn) {
        var me = this,
            store = me.getStore('Fim.store.ImportServicesHistory'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            }));
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    switchSortingOrder: function (btn) {
        var me = this,
            store = me.getStore('Fim.store.ImportServicesHistory'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            });
            if (sortingItem) {
                if (sortingItem.direction === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.direction = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.direction = Uni.component.sort.model.Sort.ASC;
                }
            }
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    updateSortingToolbarAndResults: function() {
        var me = this,
            gridView = me.getHistoryGrid(),
            store = me.getStore('Fim.store.ImportServicesHistory');

        me.updateSortingToolbar();
        gridView.setLoading();
        store.load(function(records, operation, success) {
            gridView.setLoading(false);
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.importServiceId = menu.record.get('importServiceId');
        router.arguments.occurrenceId = menu.record.get('occurrenceId');
        if (item.action === 'viewLog') {
            route = 'administration/importservices/importservice/history/occurrence';
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    }
});