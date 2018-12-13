/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.controller.Portal
 *
 * The portal controller ({@link Uni.controller.Portal}) is responsible for combining several
 * widgets that belong to the same portal category. Each widget gets placed on the same overview
 * component. The history is handled by this controller as well.
 *
 * For now, the styling is quite minimized. Every component that is specified in a
 * {@link Uni.model.PortalItem} is placed inside of a {@link Ext.panel.Panel}.
 *
 * The breadcrumb also just consists of the portal name as a header.
 *
 * # Example usage
 *
 *     var portalMenuItem = Ext.create('Uni.model.MenuItem', {
 *                 text: 'Administration',
 *                 href: '#/administration',
 *                 portal: 'administration',
 *                 glyph: 'settings',
 *                 index: 10
 *     });
 *
 *     Uni.store.MenuItems.add(menuItem);
 *
 *     var portalItem1 = Ext.create('Uni.model.PortalItem', {
 *                 title: 'Portal item 1',
 *                 component: Ext.create('My.view.Component'),
 *                 portal: 'administration'
 *             });
 *
 *      var portalItem2 = Ext.create('Uni.model.PortalItem', {
 *                title: 'Portal item 2',
 *                component: Ext.create('Ext.Component', {
 *                    html: '<h1>Test</h1>'
 *                }),
 *                portal: 'administration'
 *            });
 *
 *     Uni.store.PortalItems.add(
 *         portalItem1, portalItem2, portalItem3,
 *         portalItem4, portalItem5, portalItem6
 *     );
 *
 */
Ext.define('Uni.controller.Portal', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.MenuItems',
        'Uni.store.PortalItems',
        'Uni.view.container.PortalContainer'
    ],

    portalViews: [],

    init: function () {
        this.initMenuItems();
        this.refreshPortals();

        this.addEvents(
            /**
             * @event changemaincontentevent
             *
             * Fires after a portal view needs to be shown.
             *
             * @param {Ext.Component} Content component
             * @param {Ext.Component} Side component
             */
            'changemaincontentevent',
            /**
             * @event changemainbreadcrumbevent
             *
             * Fires after a portal breadcrumb needs to be shown.
             *
             * @param {Uni.model.BreadcrumbItem} Breadcrumb to show
             */
            'changemainbreadcrumbevent'
        );
    },

    initMenuItems: function () {
        Uni.store.MenuItems.on({
            add: this.refreshPortals,
            load: this.refreshPortals,
            update: this.refreshPortals,
            remove: this.refreshPortals,
            bulkremove: this.refreshPortals,
            scope: this
        });
    },

    refreshPortals: function () {
        var me = this,
            store = Uni.store.MenuItems;

        store.each(function (item) {
            var portal = item.get('portal'),
                title = item.get('text');

            if (!Ext.isEmpty(portal)) {
                crossroads.addRoute('/' + portal, function () {
                    me.showPortalOverview(portal, title);
                });
            }
        });
    },

    showPortalOverview: function (portal, title) {
        var store = Uni.store.PortalItems,
            portalView = this.portalViews[portal];

        if (Ext.isDefined(portalView)) {
            portalView.removeAll();
        }

        this.portalViews[portal] = Ext.create('Uni.view.container.PortalContainer', {
            title: title
        });
        portalView = this.portalViews[portal];

        store.clearFilter();
        store.filter('portal', portal);
        var portalItemsToDisplay = {};
        for (var item = store.count() - 1; item >= 0; item--) {
            var portalItem = store.getAt(item);
            if (portalItemsToDisplay.hasOwnProperty(portalItem.get('title'))) {
                Ext.each(portalItem.get('items'), function (item) {
                    portalItemsToDisplay[portalItem.get('title')].get('items').push(item);
                });
                store.remove(portalItem);
            } else {
                portalItemsToDisplay[portalItem.get('title')] = portalItem;
            }
            portalItemsToDisplay[portalItem.get('title')].get('items').sort(function (item1, item2) {
                if (item1.text < item2.text) {
                    return -1;
                } else if (item1.text > item2.text) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }
        ;

        var sortArrayForPortalItems = [];
        for (portalItemToDisplay in portalItemsToDisplay) {
            if (portalItemsToDisplay.hasOwnProperty(portalItemToDisplay)) {
                sortArrayForPortalItems.push(portalItemToDisplay);
            }
        }
        sortArrayForPortalItems.sort();
        for (var i = 0; i < sortArrayForPortalItems.length; i++) {
            portalView.addPortalItem(portalItemsToDisplay[sortArrayForPortalItems[i]]);
        }

        this.getApplication().fireEvent('changemaincontentevent', portalView);

        var portalBreadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
            text: title
        });

        this.getApplication().fireEvent('changemainbreadcrumbevent', portalBreadcrumb);
    }
});