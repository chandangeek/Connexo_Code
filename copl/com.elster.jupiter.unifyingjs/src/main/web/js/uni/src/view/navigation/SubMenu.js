/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.SubMenu
 *
 * Common submenu that supports adding buttons and toggling.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * How and where content is shown after clicking a button in the submenu is free to choose.
 * Switching between panels can easily be done using a card layout.
 * Toggling is done automatically: when the url changes, the button with the current href is selected.
 *
 *
 * # Example usage
 *
 *     @example
 *       side: [
 *          {
 *            xtype: 'navigationSubMenu',
 *            itemId: 'myMenu'
 *          }
 *        ],
 */
Ext.define('Uni.view.navigation.SubMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.navigationSubMenu',
    floating: false,
    ui: 'side-menu',
    plain: true,
    width: 256,

    defaults: {
        xtype: 'menuitem',
        hrefTarget: '_self'
    },

    selectedCls: 'current',

    initComponent: function () {
        var me = this;
        Ext.util.History.addListener('change', function (token) {
            me.checkNavigation(token);
        });
        this.callParent(this);
    },

    toggleMenuItem: function (index) {
        var cls = this.selectedCls;
        var item = this.items.getAt(index);
        if (item.hasCls(cls)) {
            item.removeCls(cls);
        } else {
            item.addCls(cls);
        }
    },

    cleanSelection: function () {
        var cls = this.selectedCls;
        this.items.each(function (item) {
            item.removeCls(cls);
        });
    },

    checkNavigation: function (token) {
        var me = this;
        me.items.each(function (item, index) {
            if ((item.href != null) && (Ext.String.endsWith(item.href, token))) {
                me.cleanSelection();
                me.toggleMenuItem(index);
            }
        });
    }
});