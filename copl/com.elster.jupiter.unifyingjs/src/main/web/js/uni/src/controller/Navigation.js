/**
 * @class Uni.controller.Navigation
 */
Ext.define('Uni.controller.Navigation', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus',
        'Uni.store.MenuItems',
        'Uni.store.AppItems',
        'Uni.store.Apps',
        'Uni.view.container.ContentContainer',
        'Uni.controller.history.Router'
    ],

    views: [],

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
        },
        {
            ref: 'searchButton',
            selector: 'navigationHeader #globalSearch'
        },
        {
            ref: 'helpButton',
            selector: 'navigationHeader #global-online-help'
        },
        {
            ref: 'onlineHelpButton',
            selector: 'navigationHeader #global-online-help'
        }
    ],

    applicationTitle: 'Connexo',
    applicationTitleSeparator: '-',
    searchEnabled: true, //Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication']),
    onlineHelpEnabled: true,
    skipRefresh: false,

    init: function (app) {
        var me = this;

        Ext.util.History.addListener('change', function () {
            me.selectMenuItemByActiveToken();
        });

        me.initApps();
        me.initMenuItems();

        me.control({
            'navigationMenu': {
                afterrender: me.onAfterRenderNavigationMenu
            },
            'navigationAppSwitcher': {
                afterrender: me.resetAppSwitcherState
            },
            'navigationHeader #globalSearch': {
                afterrender: me.initSearch
            },
            'navigationHeader #global-help-menu-btn': {
                afterrender: me.initHelpButton
            }
        });

        me.getApplication().on('changemaincontentevent', me.showContent, me);
        me.getApplication().on('changemainbreadcrumbevent', me.setBreadcrumb, me);
        me.getApplication().on('onnavigationtitlechanged', me.onNavigationTitleChanged, me);
        me.getApplication().on('onnavigationtogglesearch', me.onNavigationToggleSearch, me);
        me.getApplication().on('onnavigationtogglehelp', me.onNavigationToggleHelp, me);

        me.getController('Uni.controller.history.Router').on('routechange', me.updateBreadcrumb, me);
    },

    initApps: function () {
        Uni.store.Apps.load();
    },

    onNavigationTitleChanged: function (title) {
        this.applicationTitle = title;
    },
    onNavigationToggleSearch: function (enabled) {
        this.searchEnabled = enabled;
    },
    onNavigationToggleHelp: function (enabled) {
        this.onlineHelpEnabled = enabled;
    },

    initTitle: function (breadcrumbItem) {
        var me = this,
            text = '';

        if (Ext.isObject(breadcrumbItem)) {
            text = breadcrumbItem.get('text');

            while (Ext.isDefined(breadcrumbItem.getAssociatedData()['Uni.model.BreadcrumbItem'])) {
                breadcrumbItem = breadcrumbItem.getChild();
                text = breadcrumbItem.get('text');
            }
        }

        me.setTitle(text);
    },

    initBreadcrumbs: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var breadcrumbs = me.getBreadcrumbs();
        var child, breadcrumb;

        if(breadcrumbs.skipForNextCall === true) {
            breadcrumbs.setSkipForNextCall(false);
            return;
        }

        _.map(router.buildBreadcrumbs(), function (route) {
            var title = route.getTitle();

            if (route.route !== 'tab/:tab:') {
                breadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
                    key: route.key.replace('/', '.'),
                    text: Ext.isString(title) ? title : '',
                    href: route.buildUrl(),
                    relative: false
                });

                if (child) {
                    breadcrumb.setChild(child);
                }
                child = breadcrumb;
            }
        });

        me.initTitle(breadcrumb);
        breadcrumbs.setBreadcrumbItem(breadcrumb);
    },

    updateBreadcrumb: function(route) {
        var me = this,
            breadcrumbs = me.getBreadcrumbs(),
            breadcrumb = breadcrumbs.down('breadcrumbLink[key='+ route.key.replace('/','.') +']'),
            text = route.getTitle();
        if (breadcrumb) {
            if(breadcrumbs.items.items[breadcrumbs.items.items.length -1].text === breadcrumb.text) {
                me.setTitle(text);
            }
            breadcrumb.setText(Ext.htmlEncode(text));
        }
    },

    setTitle: function(text) {
        var me = this;
        if (!Ext.isEmpty(text)) {
            Ext.getDoc().dom.title = text + ' '
                + me.applicationTitleSeparator + ' '
                + me.applicationTitle;
        } else {
            Ext.getDoc().dom.title = me.applicationTitle;
        }
    },


    initSearch: function () {
        var me = this;
        me.getSearchButton().setVisible(me.searchEnabled);// &&
//        Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication']));
    },

    initHelpButton: function () {
        var me = this;
        me.getHelpButton().setVisible(me.onlineHelpEnabled);
        if(me.onlineHelpEnabled){
            Ext.Ajax.request({
                url: '/api/usr/currentuser',
                success: function (response) {
                    var currentUser = Ext.decode(response.responseText, true),
                        url;

                    if (currentUser && currentUser.language && currentUser.language.languageTag) {
                        url = 'help/' + currentUser.language.languageTag + '/index.html';
                        Ext.Ajax.request({
                            url: url,
                            method: 'HEAD',
                            success: function (response) {
                                me.getHelpButton().href = url;
                            }
                        });
                    }
                }
            });

        }
        me.getHelpButton()
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

        if (!this.skipRefresh && menu !== undefined) {
            this.skipRefresh = true; // skip this method while removing duplicates in the next line
            this.removeDuplicatesFromStore(store);
            this.skipRefresh = false;
            if (menu.rendered) {
                Ext.suspendLayouts();
            }

            menu.removeAllMenuItems();
            store.each(function (record) {
                menu.addMenuItem(record);
            });

            if (menu.rendered) {
                Ext.resumeLayouts(true);
            }
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
            tokens = me.stripAndSplitToken(token),
            text;

        me.getNavigationMenu().deselectAllMenuItems();

        Uni.store.MenuItems.each(function (model) {
            modelTokens = me.stripAndSplitToken(model.get('href'));
            if (tokens[0] === modelTokens[0] || tokens[0] === model.get('portal')) {
                me.getNavigationMenu().selectMenuItem(model);

                if (tokens.length === 1) {
                    text = model.get('text');

                    if (!Ext.isEmpty(text)) {
                        Ext.getDoc().dom.title = text + ' '
                            + me.applicationTitleSeparator + ' '
                            + me.applicationTitle;
                    } else {
                        Ext.getDoc().dom.title = me.applicationTitle;
                    }
                }

                return;
            }
        });
    },

    stripAndSplitToken: function (token) {
        if (token) {
            token = token.indexOf(Uni.controller.history.Settings.tokenDelimiter) === 0 ? token.substring(1) : token;
            token = token.replace(/#\/|#/g, ''); // Regex to replace all '#' or '#/'.

            // Strip the query parameters if necessary.
            if (token.indexOf('?') >= 0) {
                token = token.slice(0, token.indexOf('?'));
            }

            return token.split(Uni.controller.history.Settings.tokenDelimiter);
        } else {
            return [];
        }
    },

    showContent: function (content, side) {
        var panel = this.getContentWrapper();

        panel.removeAll();

        if (content instanceof Uni.view.container.ContentContainer) {
            side = content.side;
            content = content.content;
        }

        var contentContainer = new Ext.widget('contentcontainer', {
            content: content,
            side: side
        });

        panel.add(contentContainer);
        Ext.resumeLayouts();
    },

    setBreadcrumb: function (breadcrumbItem) {
        var trail = this.getBreadcrumbs();
        trail.setBreadcrumbItem(breadcrumbItem);
    }
});