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
        'Fim.model.LogFilter'

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
            view;

        me.setDefaultSort(router.filter);
        store.setFilterModel(router.filter);
        store.getProxy().setUrl(router.arguments);
        importServiceModel.load(importServiceId, {
            success: function (record) {
                view = Ext.widget('fim-history-log-setup', {
                    router: router,
                    importService: record

                });
                view.down('#mnu-histoty-log #import-service-view-link').setText(record.get('name'));
                me.getApplication().fireEvent('changecontentevent', view);
                me.getHistoryLogViewMenu().setTitle(showImportService ? Uni.I18n.translate('general.importHistory', 'FIM', 'Import history') : Uni.I18n.translate('general.importService', 'FIM', 'Import services'));

                me.getApplication().fireEvent('importserviceload', record);
                me.setSortingToolbar(filter);
            }
        });


    },

    setSortingToolbar: function (filter) {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer();

        sortContainer.removeAll();
        sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.value) {
                    menuItem = me.getPage().down('#menu-history-log-sort [name=' + sortItem.type + ']');
                    cls = sortItem.value === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    //menuItem.hide();
                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'history-sort-by-' + sortItem.type + '-button',
                        text: menuItem.text,
                        sortType: sortItem.type,
                        sortDirection: sortItem.value,
                        iconCls: cls/*,
                         listeners: {
                         sortCloseclick: function () {
                         this.fireEvent('removeSort', this.sortType, this.sortDirection);
                         },
                         switchSort: function () {
                         this.fireEvent('changeSortDirection', this.sortType, this.sortDirection);
                         }
                         }*/
                    });
                }
            });

        }


    },

    setDefaultSort: function (filter) {
        var sorting = filter.get('sorting');

        if (sorting === '') {
            sorting = [];

            sorting.push({
                type: 'level',
                value: Uni.component.sort.model.Sort.DESC
            });
            sorting.push({
                type: 'timestamp',
                value: Uni.component.sort.model.Sort.DESC
            });
            filter.set('sorting', sorting);
        }
    },

    chooseSort: function (menu, item) {
        var page = this.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            name = item.name,
            router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            sorting = filter.get('sorting');


        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.type === name
            });

            if (sortingItem) {
                return;
            }
            else {
                sorting.push({
                    type: name,
                    value: Uni.component.sort.model.Sort.DESC
                });
            }
        }
        else {
            sorting = [
                {
                    type: name,
                    value: Uni.component.sort.model.Sort.DESC
                }
            ];
        }
        filter.set('sorting', sorting);
        filter.save();
    },

    clearSort: function (btn) {

        var router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            sorting = filter.get('sorting');

        sorting = [];
        filter.set('sorting', sorting);
        filter.save();
    },

    sortCloseclick: function (btn) {

        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting');

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.type === btn.sortType
            }));
        }
        filter.set('sorting', sorting);
        filter.save();
    },

    switchSort: function (btn) {

        var filter = this.getController('Uni.controller.history.Router').filter,
            sorting = filter.get('sorting'),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.type === btn.sortType
            });
            if (sortingItem) {
                if (sortingItem.value === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.value = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.value = Uni.component.sort.model.Sort.ASC;
                }
            }
        }
        filter.set('sorting', sorting);
        filter.save();

    }

});