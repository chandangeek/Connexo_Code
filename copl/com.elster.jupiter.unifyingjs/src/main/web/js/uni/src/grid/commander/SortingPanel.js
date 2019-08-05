/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.commander.SortingPanel
 */
Ext.define('Uni.grid.commander.SortingPanel', {
    extend: 'Uni.grid.commander.CommanderPanel',
    xtype: 'uni-grid-commander-sortingpanel',
    requires: [
        'Uni.component.sort.model.Sort'
    ],
    mixins: [
        'Ext.util.Bindable',
    ],
    layout: 'hbox',
    defaults: {
        xtype: 'sort-item-btn',
        sortOrder: Uni.component.sort.model.Sort.DESC
    },

    initStore: function(menuItems) {
        var me = this;

        me.sortStore = Ext.create('Ext.data.Store', {
            fields: ['property', 'direction'],
            data: me.items.filter(function(item) {
                return menuItems.find(function(menuItem) {
                    return item.property === menuItem.name
                })
            })
        });
        me.sortStore.on('update', me.syncStore, me);
        me.sortStore.on('datachanged', me.syncStore, me);
        me.items = [];
    },

    initComponent: function () {
        var me = this;

        me.menu = Ext.widget(me.menu);
        me.initStore(me.menu.items.getRange());

        me.dockedItems = [
            {
                dock: 'left',
                xtype: 'label',
                text: 'Sort',
                width: 128
            },
            {
                xtype: 'toolbar',
                dock: 'right',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'button-add-sort',
                        text: 'Add sort',
                        menu: me.menu
                    },
                    {
                        itemId: 'reset',
                        xtype: 'button',
                        action: 'clear',
                        text: Uni.I18n.translate('general.clearAll','UNI','Clear all'),
                        handler: me.clearAll,
                        scope: me
                    }
                ]
            }

        ]

        me.callParent(arguments);
        me.bindStore(me.store);
        me.syncStore();

        me.menu.on('click', me.handleMenuClick, me);
    },

    syncLayout: function () {
        var me = this;
        var menuItems = me.menu.items.getRange();
        Ext.suspendLayouts();

        me.removeAll();

        menuItems.forEach(function(menuItem) {
            menuItem.setDisabled(false);
        })

        me.sortStore.each(function(item) {
            var menuItem = menuItems.find(function(menuItem) {
                return item.get('property') === menuItem.name
            });

            menuItem.setDisabled(true);

            var iconCls = item.get('direction') === Uni.component.sort.model.Sort.ASC
                ? 'x-btn-sort-item-asc'
                : 'x-btn-sort-item-desc';

            me.add({
                scope: me,
                listeners: {
                    click: function () {
                        me.toggleSort(item.get('property'));
                    },
                    closeclick: function() {
                        me.removeSort(item.get('property'));
                    },
                },
                text: menuItem.text,
                iconCls: iconCls,
            })
        });

        me.down('#button-add-sort').setDisabled(menuItems.every(function(menuItem) {
            return menuItem.isDisabled();
        }))

        me.down('button[action="clear"]')
            .setDisabled(!me.items.getRange().length)

        Ext.resumeLayouts(true);
    },

    syncStore: function () {
        var me = this;
        var items = me.sortStore.getRange();

        var sorting = items.map(function(item) {
            return {
                property: item.get('property'),
                direction: item.get('direction'),
            }
        })

        me.store.getProxy()
            .setExtraParam('sort', Ext.JSON.encode(sorting));

        me.syncLayout();
    },

    clearAll: function () {
        var me = this;
        me.sortStore.removeAll();
        me.store.load();
    },

    removeSort: function (property) {
        var me = this;
        me.sortStore.removeAt(me.sortStore.find('property', property));
        me.store.load();
    },

    toggleSort: function (property) {
        var me = this;
        var property = me.sortStore.findRecord('property', property);
        property.set('direction', property.get('direction') === Uni.component.sort.model.Sort.ASC
            ? Uni.component.sort.model.Sort.DESC
            : Uni.component.sort.model.Sort.ASC
        );
        me.store.load();
    },

    addSort: function (property) {
        var me = this;
        var property = me.sortStore.add({
            property: property,
            direction: Uni.component.sort.model.Sort.DESC,
        });
        me.store.load();
    },

    handleMenuClick: function(menu, item) {
        if (!item) {
            return false;
        }

        this.addSort(item.name);
    }
});