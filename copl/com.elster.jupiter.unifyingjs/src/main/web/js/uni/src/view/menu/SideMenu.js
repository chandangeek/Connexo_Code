/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.menu.SideMenu
 *
 * Common side menu that supports adding buttons and toggling.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * How and where content is shown after clicking a button in the side menu is free to choose.
 * Switching between panels can easily be done using a card layout.
 * Toggling is done automatically: when the url changes, the button with the current href is selected.
 *
 *
 * # Example menu configuration
 *
 *     @example
 *       me.menuItems = {
 *             text: mRID,
 *             itemId: 'deviceOverviewLink',
 *             href: '#/devices/' + mRID
 *       },
 *       {
 *             title: 'Data sources',
 *             xtype: 'menu',
 *             items: [
 *               {
 *                   text: Uni.I18n.translate('devicemenu.loadProfiles', 'UNI', 'Load profiles'),
 *                   itemId: 'loadProfilesLink',
 *                   href: '#/devices/' + mRID + '/loadprofiles',
 *                   showCondition: me.device.get('hasLoadProfiles')
 *               },
 *               {
 *                   text: Uni.I18n.translate('devicemenu.channels', 'UNI', 'Channels'),
 *                   itemId: 'channelsLink',
 *                   href: '#/devices/' + mRID + '/channels',
 *                   showCondition: me.device.get('hasLoadProfiles')
 *               }
 *           ]
 *       };
 *
 *
 * # Example usage of the menu in a component
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
    ui: 'menu-side',
    plain: true,
    width: 256,
    router: null,

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

    activeItemCls: 'item-active',

    /**
     * @cfg menuItems
     * @deprecated Use items instead.
     */
    menuItems: [],

    /**
     * @cfg showCondition
     * @deprecated Use privileges instead.
     */
    showCondition: 'showCondition',

    /**
     * @cfg uniqueMenuId
     *
     * Used for save state of submenus. Should be unique per app.
     */
    uniqueMenuId: null,

    expandedSubMenus: [],

    /**
     * @cfg objectType
     */
    objectType: null,

    subMenuDefaults: {
        xtype: 'menu',
        floating: false,
        plain: true,
        ui: 'menu-side-sub',
        titleCollapse: true,
        animCollapse: false,
        defaults: {
            xtype: 'menuitem',
            hrefTarget: '_self'
        }
    },

    initComponent: function () {
        var me = this;

        me.expandedSubMenus = Ext.decode(Ext.util.Cookies.get(me.getMenuCookieKey())) || [];

        // Selects the correct item whenever the URL changes over time.
        Ext.util.History.on('change', me.updateSelection, me);

        // backward compatibility
        if (!Ext.isEmpty(me.menuItems)) {
            me.items = me.menuItems;
        }

        me.buildHeader();

        me.callParent(arguments);

        me.on('destroy', me.onDestroy, me, {single: true});
    },

    onDestroy: function () {
        var me = this;

        Ext.util.History.un('change', me.updateSelection, me);
    },

    add: function (items) {
        var me = this;

        me.adjustItems(items)
        me.callParent(arguments);
        setTimeout(function () {
            me.updateSelection();
        }, 1);
    },

    adjustItems: function (items) {
        var me = this;

        if (Ext.isArray(items)) {
            _.each(items, prepareItem);
        } else {
            prepareItem(items);
        }

        function prepareItem(item) {
            if (!Ext.isEmpty(item.items)) {
                // item is menu
                me.adjustItems(item.items);
                me.adjustSubmenu(item);
            } else {
                me.adjustItem(item);
            }
        }
    },

    adjustItem: function (item) {
        var me = this,
            condition = item[me.showCondition];

        if (!Ext.isDefined(condition)
            || (Ext.isFunction(condition) && condition())
            || condition) {

            item.htmlEncode = !Ext.isDefined(item.htmlEncode);
            item.text = item.htmlEncode ? Ext.String.htmlEncode(item.text) : item.text;

            if (me.router && item.route) {
                var route = me.router.getRoute(item.route);
                item.href = route.buildUrl();
                item.text = item.htmlEncode ? Ext.String.htmlEncode(item.text || route.getTitle()) : item.text || Ext.String.htmlEncode(route.getTitle());
            }

            item.tooltip = item.htmlEncode ? item.text : Ext.util.Format.stripTags(item.text);
        } else {
            item.privileges = false;
        }
    },

    adjustSubmenu: function (menuConfig) {
        var me = this;

        menuConfig = Ext.applyIf(menuConfig, me.subMenuDefaults);
        if (!Ext.isEmpty(menuConfig.title)) {
            menuConfig.header = {
                autoEl: {
                    'data-qtip': menuConfig.htmlEncode ? menuConfig.title : Ext.util.Format.stripTags(menuConfig.title)
                }
            };
            menuConfig.collapsible = true;
            menuConfig.collapsed = !_.contains(me.expandedSubMenus, menuConfig.title);
            menuConfig.listeners = {
                expand: {
                    scope: me,
                    fn: me.saveSubMenuState
                },
                collapse: {
                    scope: me,
                    fn: me.removeSubMenuState
                }
            };
        }
    },

    saveSubMenuState: function (menu) {
        var me = this;

        me.expandedSubMenus.push(menu.title);
        me.updateSubMenusState();
    },

    removeSubMenuState: function (menu) {
        var me = this;

        me.expandedSubMenus = _.without(me.expandedSubMenus, menu.title);
        me.updateSubMenusState();
    },

    updateSubMenusState: function () {
        var me = this;

        Ext.util.Cookies.set(me.getMenuCookieKey(), Ext.encode(me.expandedSubMenus),
            new Date(new Date().getTime() + 365.25 * 24 * 60 * 60 * 1000)); // Expires in a year.
    },

    getMenuCookieKey: function () {
        var me = this,
            namespace = Uni.util.Application.getAppNamespace();

        namespace = namespace.replace(/\s+/g, '-').toLowerCase();

        return namespace + '-' + (me.uniqueMenuId || me.$className);
    },

    updateSelection: function () {
        var me = this;

        Ext.suspendLayouts();
        me.clearSelection();
        me.selectAppropriateItem();
        Ext.resumeLayouts();
    },

    clearSelection: function () {
        var me = this,
            selectedItem;

        if (me.rendered) {
            selectedItem = me.getEl().down('.' + me.activeItemCls);
            if (selectedItem) {
                selectedItem.removeCls(me.activeItemCls);
            }
        }
    },

    selectAppropriateItem: function () {
        var me = this,
            itemForSelect = me.getItemForSelect(),
            menu;

        if (itemForSelect) {
            menu = itemForSelect.up('menu');
            itemForSelect.addCls(me.activeItemCls);
            if (menu) {
                if (menu.rendered) {
                    menu.suspendEvent('expand');
                    setTimeout(function () {
                        menu.expand();
                        menu.resumeEvent('expand');
                    }, 1)
                } else {
                    menu.collapsed = false;
                }
            }
        }
    },

    /**
     * Checks which item is best to be selected based on the currently selected URL.
     */
    getItemForSelect: function () {
        var me = this,
            fullToken = '#' + Ext.util.History.getToken(),
            selection;

        _.each(me.query('menuitem'), setSelection);

        return selection ? selection.item : selection;

        function setSelection(item) {
            if (!Ext.isEmpty(item.href) && Ext.String.startsWith(fullToken, item.href)) {
                selection = pickBestSelection(selection, {
                    item: item,
                    fitness: fullToken.length - item.href.length
                });
            }
        }

        function pickBestSelection(a, b) {
            if (!Ext.isDefined(b)) {
                return a;
            } else if (!Ext.isDefined(a)) {
                return b
            }

            if (a.fitness <= b.fitness) {
                return a
            } else if (a.fitness > b.fitness) {
                return b
            }
        }
    },

    // backward compatibility
    addMenuItems: function () {
        var me = this;

        me.add.apply(me, arguments);
    },

    buildHeader: function () {
        var me = this;
        me.header = {
            xtype: 'panel',
            height: 50,
            maxWidth: 223,
            items: [{
                xtype: 'component',
                itemId: 'side-menu-header-object-type',
                cls: 'x-menu-header-object-type',
                html: Ext.htmlEncode(me.objectType)
            }, {
                xtype: 'component',
                itemId: 'side-menu-header-object-name',
                cls: 'x-menu-header-object-name',
                html: Ext.htmlEncode(me.title)
            }],
            title: false
        };
    },

    setHeader: function (title) {
        var me = this;
        if (me.rendered) {
            if (title) {
                me.down('#side-menu-header-object-name').update(Ext.htmlEncode(title));
            }
            me.updateLayout();
        } else {
            me.title = title;
            me.buildHeader();
        }
    }
});