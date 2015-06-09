Ext.define('Fim.controller.Log', {
    extend: 'Ext.app.Controller',
    requires: [
        'Fim.privileges.DataImport'
    ],
    views: [
        'Fim.view.log.Setup'
    ],
    stores: [
        'Fim.store.Logs'

    ],
    models: [
        'Fim.model.LogFilter',
        'Fim.model.ImportServiceHistory'

    ],
    refs: [
        {
            ref: 'page',
            selector: 'fim-history-log-setup'
        },
        {
            ref: 'historyLogViewMenu',
            selector: '#pnl-histoty-log-menu'
        }
    ],
    applicationName: null,

    init: function () {
        this.control({
            'fim-history-log-sort-menu': {
                click: this.chooseSort
            },
            '#fim-history-log-sort-toolbar button[action=clear]': {
                click: this.clearSort
            },
            '#fim-history-log-sort-toolbar button': {
                closeclick: this.sortCloseclick
            },
            '#fim-history-log-sort-toolbar #itemsContainer button': {
                click: this.switchSort
            }
        });

        applicationName = typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : '';
    },

    showImportServicesHistoryLog: function (importServiceId, occurrenceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filter = router.filter,
            showImportService = (occurrenceId === undefined),
            importServiceId = router.arguments.importServiceId,
            occurrenceId = router.arguments.occurrenceId,
            store = me.getStore('Fim.store.Logs'),
            importServiceModel = me.getModel('Fim.model.ImportService'),
            importServiceHistory = me.getModel('Fim.model.ImportServiceHistory'),
            view;

        me.setDefaultSort(router.filter);
        store.setFilterModel(router.filter);
        store.getProxy().setUrl(router.arguments);
        importServiceModel.load(importServiceId, {
            success: function (record) {

                importServiceHistory.load(occurrenceId, {
                    success: function (occurrenceTask) {

                        view = Ext.widget('fim-history-log-setup', {
                            router: router,
                            importService: record

                        });
                        view.down('#mnu-histoty-log').setTitle(record.get('name'));
                        me.getApplication().fireEvent('changecontentevent', view);
                        me.getHistoryLogViewMenu().setTitle(showImportService ? Uni.I18n.translate('general.importHistory', 'FIM', 'Import history') : Uni.I18n.translate('general.importService', 'FIM', 'Import services'));

                        view.down('#frm-history-log-preview').loadRecord(occurrenceTask);
                        view.down('#run-started-on').setValue(occurrenceTask.get('startedOnDisplay'));

                        me.getApplication().fireEvent('importserviceload', record);
                        me.setSortingToolbar(filter);
                    }

                });
            }
        });


    },

    setSortingToolbar: function (filter) {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            store = me.getStore('Fim.store.Logs');

        sortContainer.removeAll();
        sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    menuItem = me.getPage().down('#menu-history-log-sort [name=' + sortItem.property + ']');
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

    setDefaultSort: function (filter) {
        var me = this,
            store = me.getStore('Fim.store.Logs'),
            sorting = store.getProxy().extraParams['sort'];

        if (sorting === undefined) {
            sorting = [];

            sorting.push({
                property: 'level',
                direction: Uni.component.sort.model.Sort.DESC
            });
            sorting.push({
                property: 'timestamp',
                direction: Uni.component.sort.model.Sort.DESC
            });
            store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        }
    },

    chooseSort: function (menu, item) {
        var me = this,
            name = item.name,
            router = me.getController('Uni.controller.history.Router'),
            filter = router.filter,
            store = me.getStore('Fim.store.Logs'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);


        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.property === name
            });

            if (sortingItem) {
                return;
            }
            else {
                sorting.push({
                    property: name,
                    direction: Uni.component.sort.model.Sort.DESC
                });
            }
        }
        else {
            sorting = [
                {
                    property: name,
                    direction: Uni.component.sort.model.Sort.DESC
                }
            ];
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        filter.save();
    },

    clearSort: function (btn) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filter = router.filter,
            store = me.getStore('Fim.store.Logs');

        sorting = [];
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        filter.save();
    },

    sortCloseclick: function (btn) {
        var me = this,
            filter = me.getController('Uni.controller.history.Router').filter,
            store = me.getStore('Fim.store.Logs'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            }));
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        filter.save();
    },

    switchSort: function (btn) {
        var me = this,
            filter = me.getController('Uni.controller.history.Router').filter,
            store = me.getStore('Fim.store.Logs'),
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
        filter.save();
    }
});