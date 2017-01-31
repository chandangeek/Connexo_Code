/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.controller.OverviewSorting', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.readingtypes.view.Overview',
        'Mtr.readingtypes.view.Grid',
        'Mtr.readingtypes.view.EditAliasWindow'
    ],

    requires: [

    ],

    stores: [
        'Mtr.readingtypes.store.ReadingTypes'
    ],


    refs: [
        {
            ref: 'page',
            selector: '#reading-types-setup'
        },
        {
            ref: 'readingTypesGrid',
            selector: '#metering-reading-types-grid'
        }
    ],

    init: function () {
        this.control({
            'reading-types-sorting-menu': {
                click: this.chooseSort
            },
            '#reading-types-sorting-toolbar button[action=clear]': {
                click: this.clearAllSorting
            },
            '#reading-types-sorting-toolbar button': {
                closeclick: this.onSortCloseClicked
            },
            '#reading-types-sorting-toolbar #itemsContainer button': {
                click: this.switchSortingOrder
            }
        });
    },

    chooseSort: function (menu, item) {
        var me = this,
            name = item.name,
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        if (Ext.isArray(sorting)) {
            var sortingItem = Ext.Array.findBy(sorting, function (item) {
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

    updateSortingToolbarAndResults: function() {
        var me = this,
            gridView = me.getReadingTypesGrid(),
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes');

        me.updateSortingToolbar();
        gridView.setLoading();
        store.load(function(records, operation, success) {
            gridView.setLoading(false);
        });
    },

    updateSortingToolbar: function () {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes');

        sortContainer.removeAll();
        var sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);


        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    var menuItem = me.getPage().down('#reading-types-sorting-menu [name=' + sortItem.property + ']');
                    var cls = sortItem.direction === Uni.component.sort.model.Sort.ASC
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

    onSortCloseClicked: function (btn) {
        var me = this,
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes'),
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
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes'),
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

    clearAllSorting: function (btn) {
        var me = this,
            sorting = [],
            store = me.getStore('Mtr.readingtypes.store.ReadingTypes');

        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    }
});

