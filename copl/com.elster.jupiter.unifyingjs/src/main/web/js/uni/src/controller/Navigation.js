/**
 * @class Uni.controller.Navigation
 */
Ext.define('Uni.controller.Navigation', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus',
        'Uni.store.MenuItems',
        'Uni.store.AppItems',
        'Uni.view.container.ContentContainer',
        'Uni.controller.history.Router'
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
        },
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        }
    ],

    init: function () {
        var me = this;

        Ext.util.History.addListener('change', function () {
            me.selectMenuItemByActiveToken();
        });

        this.initMenuItems();

        this.control({
            'navigationMenu': {
                afterrender: this.onAfterRenderNavigationMenu
            },
            'navigationAppSwitcher': {
                afterrender: this.resetAppSwitcherState
            }
        });

        this.getApplication().on('changemaincontentevent', this.showContent, this);
        this.getApplication().on('changemainbreadcrumbevent', this.setBreadcrumb, this);
        this.getController('Uni.controller.history.Router').on('routematch', this.initBreadcrumbs, this);
        this.getController('Uni.controller.history.Router').on('routeChange', this.initBreadcrumbs, this);
    },

    initBreadcrumbs: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var breadcrumbs = me.getBreadcrumbs();
        var child, breadcrumb;

        breadcrumbs.removeAll();
        _.map(router.buildBreadcrumbs(), function (route) {
            var title = route.getTitle();

            breadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
                text: Ext.isString(title) ? title : '',
                href: route.buildUrl(),
                relative: false
            });

            if (child) {
                breadcrumb.setChild(child);
            }
            child = breadcrumb;
        });
        breadcrumbs.setBreadcrumbItem(breadcrumb);
    },

    onAfterRenderNavigationMenu: function () {
        this.refreshNavigationMenu();
        this.selectMenuItemByActiveToken();
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

        this.removeDuplicatesFromStore(store);

        if (menu !== undefined) {
            menu.removeAllMenuItems();
            store.each(function (record) {
                menu.addMenuItem(record);
            });
        }
    },

    removeDuplicatesFromStore: function (store) {
        var hits = [],
            duplicates = [];

        store.each(function (record) {
            var text = record.get('text'),
                portal = record.get('portal');

            if (hits[text + portal]) {
                duplicates.push(record);
            } else {
                hits[text + portal] = true;
            }
        });

        // Delete the duplicates.
        store.remove(duplicates);
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

    selectMenuItemByActiveToken: function () {
        var me = this,
            token = Ext.util.History.getToken(),
            tokens = me.stripAndSplitToken(token);

        me.getNavigationMenu().deselectAllMenuItems();

        Uni.store.MenuItems.each(function (model) {
            modelTokens = me.stripAndSplitToken(model.get('href'));
            if (tokens[0] === modelTokens[0] || tokens[0] === model.get('portal')) {
                me.getNavigationMenu().selectMenuItem(model);
                return;
            }
        });
    },

    stripAndSplitToken: function (token) {
        if (token) {
            token = token.indexOf(Uni.controller.history.Settings.tokenDelimiter) === 0 ? token.substring(1) : token;
            token = token.replace(/#\/|#/g, ''); // Regex to replace all '#' or '#/'.
            return token.split(Uni.controller.history.Settings.tokenDelimiter);
        } else {
            return [];
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
        var trail = this.getBreadcrumbs();
        trail.setBreadcrumbItem(breadcrumbItem);
    }
});