/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Fim.model.ImportServiceHistory',
        'Fim.model.ImportServiceDetails'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'fim-history-log-setup'
        },
        {
            ref: 'historyLogViewMenu',
            selector: '#mnu-histoty-log'
        }
    ],

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
    },

    showImportServicesHistoryLogWorkspace: function (occurrenceId) {
        this.showImportServicesHistoryLog(null, occurrenceId, true)
    },

    showImportServicesHistoryLog: function (importServiceId, occurrenceId, fromWorkSpace) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filter = router.filter,
            showImportService = (occurrenceId === undefined),
            store = me.getStore('Fim.store.Logs'),
            importServiceHistory = me.getModel('Fim.model.ImportServiceHistory'),
            view;

        me.setDefaultSort(router.filter);
        store.setFilterModel(router.filter);
        store.getProxy().setUrl(router.arguments);

        importServiceHistory.load(occurrenceId, {
            success: function (occurrenceTask) {

                view = Ext.widget('fim-history-log-setup', {
                    router: router,
                    fromWorkSpace: fromWorkSpace,
                    importServiceId: occurrenceTask.get('importServiceId')

                });
                me.getApplication().fireEvent('changecontentevent', view);

                Ext.suspendLayouts();

                view.down('#import-history-log-grid-empty-message').setText(
                    Uni.I18n.translate('importService.log.startedOnNoLogs', 'FIM', '{0} started on {1} did not create any logs',
                        [occurrenceTask.get('importServiceName'), occurrenceTask.get('startedOnDisplay')])
                );

                if (!fromWorkSpace) {
                    view.down('#mnu-histoty-log #import-service-log-view-link').setText(occurrenceTask.get('importServiceName'));
                    me.getHistoryLogViewMenu().setTitle(showImportService ? Uni.I18n.translate('general.importHistory', 'FIM', 'Import history') : Uni.I18n.translate('general.importServices', 'FIM', 'Import services'));

                } else {
                    view.down('#main-panel').setTitle(
                        Uni.I18n.translate('importService.log.of.occurence', 'FIM', "Log '{0}'", occurrenceTask.get('startedOnDisplay'))
                    );
                }
                view.down('#frm-history-log-preview').loadRecord(occurrenceTask);
                view.down('#run-started-on').setValue(occurrenceTask.get('startedOnDisplay'));

                me.getApplication().fireEvent('importserviceload', occurrenceTask.get('importServiceName'));
                me.setSortingToolbar(filter);
                Ext.resumeLayouts();
            }
        });
    },

    setSortingToolbar: function (filter) {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            store = me.getStore('Fim.store.Logs'),
            sorting,
            menuItem,
            cls;

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
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']),
            sortingItem;


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
            store = me.getStore('Fim.store.Logs'),
            sorting;

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