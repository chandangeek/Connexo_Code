/**
 * @class Uni.view.menu.SideMenu
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
Ext.define('Uni.view.menu.SideMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'uni-view-menu-side',
    floating: false,
    ui: 'side-menu', // TODO Rename to 'menu-side'.
    plain: true,
    width: 256,

    defaults: {
        xtype: 'menuitem',
        hrefTarget: '_self',
        defaults: {
            xtype: 'menuitem',
            hrefTarget: '_self'
        }
    },

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    activeItemCls: 'current', // TODO Rename to 'active-item'.

    /**
     * @cfg menuItems
     */
    menuItems: [],

    /**
     * @cfg showCondition
     */
    showCondition: 'showCondition',

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        Ext.util.History.addListener('change', function (token) {
            me.checkNavigation(token);
        });

        if (Ext.isDefined(me.menuItems) && Ext.isArray(me.menuItems)) {
            me.buildMenuItems();
        }

        me.checkNavigation(Ext.util.History.getToken());
    },

    buildMenuItems: function () {
        var me = this;

        Ext.suspendLayouts();
        me.addMenuItems(me.menuItems);
        Ext.resumeLayouts();
    },

    addMenuItems: function (menuItems) {
        var me = this;

        for (var i = 0; i < menuItems.length; i++) {
            var item = menuItems[i],
                subItems = item.items,
                condition = item[me.showCondition];

            if ((typeof item.xtype === 'undefined' || item.xtype === 'menu')
                && Ext.isDefined(subItems) && Ext.isArray(subItems)) {
                me.applyMenuDefaults(item);
                me.addSubMenuItems(item);

                // Do not bother adding the menu if there are no items in it.
                if (item.items.length === 0) {
                    continue;
                }
            }

            if (typeof condition !== 'undefined') {
                if (Ext.isFunction(condition) && condition() || condition) {
                    me.add(item);
                }

                // Do not add the item if the condition is not valid.
                continue;
            }

            me.add(item);
        }
    },

    addSubMenuItems: function (menu) {
        var me = this,
            subItems = menu.items;

        // Reset menu items.
        menu.items = [];

        for (var i = 0; i < subItems.length; i++) {
            var item = subItems[i],
                condition = item[me.showCondition];

            if (typeof condition !== 'undefined') {
                if (Ext.isFunction(condition) && condition() || condition) {
                    menu.items.push(item);
                }

                // Do not add the item if the condition is not valid.
                continue;
            }

            menu.items.push(item);
        }
    },

    applyMenuDefaults: function (config) {
        Ext.applyIf(config, {
            xtype: 'menu',
            floating: false,
            plain: true,

            layout: {
                type: 'vbox',
                align: 'stretch'
            }
        });
    },

    clearSelection: function (items) {
        var me = this,
            subItems;
        for (var i = 0; i < items.length; i++) {
            var item = items[i];

            subItems = item.items;

            item.removeCls(me.activeItemCls);

            if (Ext.isDefined(subItems) && Ext.isArray(subItems)) {
                me.clearSelection(subItems);
            }
        }
    },

    checkNavigation: function (token) {
        var me = this,
            items = me.items.items;

        Ext.suspendLayouts();

        if (Ext.isDefined(items) && Ext.isArray(items)) {
            me.clearSelection(items);
            me.checkSelectedItems(items, token);
        }

        Ext.resumeLayouts();
    },

    checkSelectedItems: function (items, token) {
        var me = this,
            href,
            subItems;

        for (var i = 0; i < items.length; i++) {
            var item = items[i];

            href = item.href;
            subItems = item.items ? item.items.items : undefined;

            if (Ext.isDefined(href) && href !== null && Ext.String.endsWith(href, '#' + token)) {
                item.addCls(me.activeItemCls);
            }

            // Recursively check the items.
            if (Ext.isDefined(subItems) && Ext.isArray(subItems)) {
                me.checkSelectedItems(subItems, token);
            }
        }
    }
});