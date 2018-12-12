/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.Menu
 */
Ext.define('Uni.view.navigation.Menu', {
    extend: 'Ext.container.Container',
    xtype: 'navigationMenu',
    ui: 'navigationmenu',

    requires: [
        'Uni.util.Application'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    baseCollapsedCookieKey: '_nav_menu_collapsed',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                flex: 1,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    xtype: 'button',
                    ui: 'menuitem',
                    hrefTarget: '_self',
                    toggleGroup: 'menu-items',
                    action: 'menu-main',
                    enableToggle: true,
                    allowDepress: false,
                    tooltipType: 'title',
                    iconAlign: 'top',
                    scale: 'large'
                }
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                padding: '4px',
                items: [
                    {
                        xtype: 'component',
                        html: '&nbsp;',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        ui: 'toggle',
                        itemId: 'toggle-button',
                        toggleGroup: 'uni-navigation-menu-toggle-button',
                        tooltipType: 'title',
                        tooltip: Uni.I18n.translate('navigation.toggle.collapse', 'UNI', 'Collapse'),
                        enableToggle: true,
                        scale: 'large',
                        toggleHandler: me.onToggleClick,
                        pressed: JSON.parse(Ext.util.Cookies.get(me.getCollapsedCookieKey())),
                        scope: me
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.on('afterrender', function () {
            me.setCollapsed(JSON.parse(Ext.util.Cookies.get(me.getCollapsedCookieKey())) || false);
        }, me);
    },

    removeAllMenuItems: function () {
        this.getMenuContainer().removeAll();
    },

    addMenuItem: function (model) {
        this.getMenuContainer().add(this.createMenuItemFromModel(model));
    },

    createMenuItemFromModel: function (model) {
        var iconCls = model.data.glyph ? 'uni-icon-' + model.data.glyph : 'uni-icon-none',
            href = model.data.portal ? '#/' + model.data.portal : model.data.href;

        return {
            tooltip: model.data.text,
            text: model.data.text,
            href: href,
            data: model,
            iconCls: iconCls,
            hidden: model.data.hidden
        };
    },

    selectMenuItem: function (model) {
        var me = this,
            itemId = model.id;

        me.getMenuContainer().items.items.forEach(function (item) {
            if (itemId === item.data.id) {
                me.deselectAllMenuItems();
                item.toggle(true, false);
            }
        });
    },

    deselectAllMenuItems: function () {
        this.getMenuContainer().items.items.forEach(function (item) {
            item.toggle(false, false);
        });
    },

    onToggleClick: function (button, state) {
        var me = this;
        me.setCollapsed(state);
    },

    setCollapsed: function (collapsed) {
        var me = this;

        if (collapsed) {
            me.collapseMenu();
        } else {
            me.expandMenu();
        }

        me.setCollapsedCookie(collapsed);
    },

    getCollapsedCookieKey: function () {
        var me = this,
            namespace = Uni.util.Application.getAppNamespace();

        namespace = namespace.replace(/\s+/g, '-').toLowerCase();

        return namespace + me.baseCollapsedCookieKey;
    },

    collapseMenu: function () {
        var me = this;

        me.getToggleButton().setTooltip(Uni.I18n.translate('navigation.toggle.expand', 'UNI', 'Expand'));

        me.addCls('collapsed');
        me.setWidth(48);
        me.doLayout();
    },

    expandMenu: function () {
        var me = this;

        me.getToggleButton().setTooltip(Uni.I18n.translate('navigation.toggle.collapse', 'UNI', 'Collapse'));

        me.removeCls('collapsed');
        me.setWidth(null);
        me.doLayout();
    },

    setCollapsedCookie: function (collapsed) {
        var me = this;

        Ext.util.Cookies.set(me.getCollapsedCookieKey(), collapsed,
            new Date(new Date().getTime() + 365.25 * 24 * 60 * 60 * 1000)); // Expires in a year.
    },

    getMenuContainer: function () {
        return this.down('container:first');
    },

    getToggleButton: function () {
        return this.down('#toggle-button');
    }
});