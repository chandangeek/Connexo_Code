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
 *                 glyph: 'xe020@icomoon',
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
            store = Uni.store.MenuItems,
            eventBus = me.getController('Uni.controller.history.EventBus');

        store.each(function (item) {
            var href = item.get('href'),
                portal = item.get('portal'),
                title = item.get('text');

            if (!Ext.isEmpty(portal)) {
                eventBus.addTokenObserver(function () {
                    me.showPortalOverview(portal, title);
                }, portal);
            }
        });
    },

    showPortalOverview: function (portal, title) {
        var store = Uni.store.PortalItems,
            mainView = Ext.create('Uni.view.container.PortalContainer');

        store.filter('portal', portal);

        store.each(function (portalItem) {
            mainView.addPortalItem(portalItem);
        });

        this.getApplication().fireEvent('changemaincontentevent', mainView);

        // TODO Make a more stylized breadcrumb.
        var portalBreadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
            text: '<h2>' + title + '</h2>'
        });

        this.getApplication().fireEvent('changemainbreadcrumbevent', portalBreadcrumb)
    }
});