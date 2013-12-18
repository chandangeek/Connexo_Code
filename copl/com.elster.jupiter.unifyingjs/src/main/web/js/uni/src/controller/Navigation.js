Ext.define('Uni.controller.Navigation', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus',
        'Uni.store.MenuItems',
        'Uni.store.AppItems'
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
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    config: {
        menuTask: undefined
    },

    init: function () {
        var me = this,
            eventBus = this.getController('Uni.controller.history.EventBus');

        eventBus.addTokenObserver(function (tokens, delimiter) {
            me.selectMenuItemByTokens(tokens, delimiter);
        }, undefined);

        this.initMenuItems();
//        this.initAppItems();

        this.control({
            'navigationMenu #menu-main': {
                afterrender: this.refreshNavigationMenu
            },
            'navigationMenu button[action=menu-main]': {
                mouseover: this.peekCollapsedMenu
            },
            'navigationAppSwitcher': {
                afterrender: this.resetAppSwitcherState
            },
            'navigationMenu': {
                afterrender: this.initNavigationEvents
            }
        });
    },

    initNavigationEvents: function (panel) {
        panel.body.on('mouseover', this.highlightMenu, this);
        panel.body.on('mouseout', this.collapseMenu, this);
    },

    initMenuItems: function () {
        Uni.store.MenuItems.on({
            add: this.refreshNavigationMenu,
            load: this.refreshNavigationMenu,
            update: this.refreshNavigationMenu,
            remove: this.refreshNavigationMenu,
            bulkremove: this.refreshNavigationMenu,
            scope: this
        });
    },

    initAppItems: function () {
        Uni.store.AppItems.on({
            add: this.resetAppSwitcherState,
            load: this.resetAppSwitcherState,
            update: this.resetAppSwitcherState,
            remove: this.resetAppSwitcherState,
            bulkremove: this.resetAppSwitcherState,
            scope: this
        });
        Uni.store.AppItems.load();
    },

    resetAppSwitcherState: function () {
        var count = Uni.store.AppItems.getCount();

        if (count > 0) {
            this.getAppSwitcher().enable();
        } else {
            this.getAppSwitcher().disable();
        }
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

//        this.getNavigationMenu().body.on('mouseover', this.cancelCurrentTask);
//        this.getNavigationMenu().body.on('mouseout', this.collapseMenu);
    },

    addMenuItem: function (title, href, glyph) {
        var item = {
            text: title,
            tooltip: title,
            href: href,
            glyph: glyph
        };

        this.getMainMenu().getHeader().add(item);
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

    peekCollapsedMenu: function (button) {
        // TODO Also force the 'hover state' appearance.
        this.getNavigationMenu().peekMenuItem(button.data.id);
    },

    showActiveMenu: function () {
        var me = this;
        console.log('showActiveMenu');
        me.cancelCurrentTask();

        var task = new Ext.util.DelayedTask(function () {
            me.getNavigationMenu().showActiveMenu();
        });
        task.delay(100);

        me.setMenuTask(task);
    },

    cancelCurrentTask: function () {
        if (this.getMenuTask() !== undefined) {
            this.getMenuTask().cancel();
            this.setMenuTask(undefined);
        }
    },

    highlightMenu: function() {
        this.cancelCurrentTask();
        this.getNavigationMenu().highlightActiveMenu();
    },

    collapseMenu: function () {
        var me = this;
        var task = new Ext.util.DelayedTask(function () {
            me.getNavigationMenu().collapseMenu();
            me.getNavigationMenu().showActiveMenu();
            me.getNavigationMenu().highlightActiveMenu();
        });

        task.delay(100);
        me.setMenuTask(task);
    }
});