/**
 * @class Uni.controller.Navigation
 */
Ext.define('Uni.controller.Navigation', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus',
        'Uni.store.MenuItems',
        'Uni.store.AppItems',
        'Uni.view.container.ContentContainer'
    ],

    views: [
    ],

    refs: [
        {
            ref: 'navigationMenu',
            selector: 'navigationMenu'
        },
        {
            ref: 'contentWrapper',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        var me = this,
            eventBus = this.getController('Uni.controller.history.EventBus');

        eventBus.addTokenObserver(function (tokens, delimiter) {
            me.selectMenuItemByTokens(tokens, delimiter);
        }, undefined);

        this.initMenuItems();
//        this.initAppItems();

        this.control({
            'navigationMenu': {
                afterrender: this.refreshNavigationMenu
            },
            'navigationAppSwitcher': {
                afterrender: this.resetAppSwitcherState
            }
        });

        this.getApplication().on('changemaincontentevent', this.showContent, this);
        this.getApplication().on('changemainbreadcrumbevent', this.setBreadcrumb, this);
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
    },

    addMenuItem: function (title, href, glyph) {
        href = portal ? '#/' + portal : href;
        var item = {
            text: title,
            tooltip: title,
            href: href,
            glyph: glyph
        };

        this.getNavigationMenu().addMenuItem(item);
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

    showContent: function (content, side) {
        this.getContentWrapper().removeAll();

        if (content instanceof Uni.view.container.ContentContainer) {
            side = content.side;
            content = content.content;
        }

        var contentContainer = new Ext.widget('contentcontainer', {
            content: content,
            side: side
        });

        this.getContentWrapper().add(contentContainer);
        this.getContentWrapper().doComponentLayout();
    },

    setBreadcrumb: function (breadcrumbItem) {
        var contentContainer = this.getContentWrapper().down('contentcontainer'),
            trail = contentContainer.getBreadcrumbTrail();
        trail.setBreadcrumbItem(breadcrumbItem);
    }
});