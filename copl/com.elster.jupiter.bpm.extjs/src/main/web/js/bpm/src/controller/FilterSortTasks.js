/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.controller.FilterSortTasks', {
    extend: 'Ext.app.Controller',
    stores: [
		'Bpm.store.task.Tasks'
    ],
    views: [
        'Uni.view.button.SortItemButton'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'bpm-tasks'
        },
        {
            ref: 'mainGrid',
            selector: 'bpm-tasks bpm-tasks-grid'
        }
    ],

    init: function () {
        this.control({
            'bpm-tasks-sort-menu': {
                click: this.chooseSort
            },
            '#bpm-task-sort-toolbar button[action=clear]': {
                click: this.clearAllSorting
            },
            '#bpm-task-sort-toolbar button': {
                closeclick: this.onSortCloseClicked
            },
            '#bpm-task-sort-toolbar #itemsContainer button': {
                click: this.switchSortingOrder
            }
        });
    },

    updateSortingToolbar: function () {
        var me = this,
            page = me.getPage(),
            sortContainer = page.down('container[name=sortitemspanel]').getContainer(),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            store = me.getStore('Bpm.store.task.Tasks');

        sortContainer.removeAll();
        sorting = Ext.JSON.decode(queryString.sort);

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    menuItem = me.getPage().down('#menu-tasks-sort [name=' + sortItem.property + ']');
                    cls = sortItem.direction === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'sort-by-' + sortItem.property + '-button',
                        text: menuItem.text,
                        sortType: sortItem.property,
                        sortDirection: sortItem.direction,
                        iconCls: cls
                    });
                }
            });
        }
    },

    chooseSort: function (menu, item) {
        var me = this,
            name = item.name,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            sorting = Ext.JSON.decode(queryString.sort);

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
        queryString.sort = Ext.JSON.encode(sorting);
        me.applyNewState(queryString);
        me.updateSortingToolbarAndResults();
    },

    clearAllSorting: function (btn) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        sorting = [];
        queryString.sort = Ext.JSON.encode(sorting);
        me.applyNewState(queryString);
        me.updateSortingToolbarAndResults();
    },

    onSortCloseClicked: function (btn) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            sorting = Ext.JSON.decode(queryString.sort);

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            }));
        }
        queryString.sort = Ext.JSON.encode(sorting);
        me.applyNewState(queryString);
        me.updateSortingToolbarAndResults();
    },

    switchSortingOrder: function (btn) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            sorting = Ext.JSON.decode(queryString.sort),
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
        queryString.sort = Ext.JSON.encode(sorting);
        me.applyNewState(queryString);
        me.updateSortingToolbarAndResults();
    },

    updateSortingToolbarAndResults: function() {
        var me = this,
            gridView = me.getMainGrid(),
            store = me.getStore('Bpm.store.task.Tasks');

        me.updateSortingToolbar();
        gridView.setLoading();
        store.load(function(records, operation, success) {
            gridView.setLoading(false);
        });
    },

    applyNewState: function (queryString) {
        var me = this,
            href = Uni.util.QueryString.buildHrefWithQueryString(queryString, false);

        if (window.location.href !== href) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.href = href;
            Ext.util.History.currentToken = window.location.hash.substr(1);
        }
    }

});