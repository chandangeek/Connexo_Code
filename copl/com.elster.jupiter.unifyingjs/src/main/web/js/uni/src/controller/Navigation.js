Ext.define('Uni.controller.Navigation', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus',
        'Uni.store.MenuItems'
    ],

    views: [
    ],

    refs: [
        {
            ref: 'navigationMenu',
            selector: 'viewport > navigationMenu'
        },
        {
            ref: 'mainMenu',
            selector: 'navigationMenu #menu-main'
        },
        {
            ref: 'navigationToggler',
            selector: 'navigationHeader > navigationToggler'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        var me = this,
            eventBus = this.getController('Uni.controller.history.EventBus');

        eventBus.addTokenObserver(function (tokens, delimiter) {
            me.selectMenuItemByTokens(tokens, delimiter);
        }, undefined);

        Uni.store.MenuItems.on({
            add: this.refreshNavigationMenu,
            update: this.refreshNavigationMenu,
            remove: this.refreshNavigationMenu,
            scope: this
        });

        this.control({
            'navigationToggler': {
                toggle: this.toggleNavigation
            },
            'navigationMenu': {
                afterrender: this.checkNavigationVisibility
            },
            'navigationMenu #menu-main': {
                beforerender: this.refreshNavigationMenu
            },
            'navigationMenu #menu-main button': {
                click: this.showSubMenu,
                mouseover: this.peekSubMenu
            }
        });
    },

    refreshNavigationMenu: function () {
        var menu = this.getNavigationMenu(),
            store = Uni.store.MenuItems;

        if (menu !== undefined) {
            menu.removeAllMenuItems();
            store.each(function (record) {
                menu.addMenuItem(record);
            });
        }
    },

    addMenuItem: function (title, href, glyph) {
        var item = {
            text: title,
            tooltip: title,
            href: href,
            glyph: glyph
        };

        this.getMainMenu().add(item);
    },

    selectMenuItemByTokens: function (tokens, delimiter) {
        var me = this,
            href = '#';

        for (var i = 0; i < tokens.length; i++) {
            var token = tokens[i];
            href += delimiter + token;

            var result = Uni.store.MenuItems.findRecord('href', href);

            if (result != null) {
                me.getNavigationMenu().selectMenuItem(result);
            }
        }
    },

    toggleNavigation: function (button, pressed) {
        // TODO Fix layout issue with collapsed menu items after refresh.
        if (pressed) {
            this.getNavigationMenu().expand();
        } else {
            this.getNavigationMenu().collapse();
        }
    },

    checkNavigationVisibility: function () {
        var button = this.getNavigationToggler();
        this.toggleNavigation(button, button.pressed);
    },

    showSubMenu: function (button) {
        var model = button.data;
        if (model.children().data.length > 0) {
            this.getNavigationMenu().expandSub();
        } else {
            this.getNavigationMenu().collapseSub();
        }
    },

    peekSubMenu: function (button) {
        // TODO Show the sub menu.
    }
});