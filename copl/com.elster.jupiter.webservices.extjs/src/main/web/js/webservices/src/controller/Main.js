/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Wss.controller.history.Webservices',
        'Wss.controller.Webservices'
    ],

    stores: [],

    init: function () {
        this.initHistorians();
        this.initMenu();

        this.callParent(arguments);
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Wss.controller.history.Webservices');
    },

    initMenu: function () {
        var namespace = Uni.util.Application.getAppNamespace();

        if (Wss.privileges.Webservices.canView()) {
            if (namespace === 'SystemApp') {
                var menuItem = Ext.create('Uni.model.MenuItem', {
                    text: Uni.I18n.translate('general.administration', 'WSS', 'Administration'),
                    portal: 'administration',
                    glyph: 'settings'
                });

                Uni.store.MenuItems.add(menuItem);

                var calendarItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.webServices', 'WSS', 'Web services'),
                    portal: 'administration',
                    items: [
                        {
                            text: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Web service endpoints'),
                            href: '#/administration/webserviceendpoints',
                            hidden: !(Uni.Auth.hasPrivilege('privilege.view.webservices')),
                            route: 'webserviceendpoints'
                        },
                        {
                            text: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service history'),
                            href: '#/administration/webservicehistory',
                            hidden: !(Uni.Auth.hasPrivilege('privilege.view.webservices')),
                            route: 'webservicehistory'
                        }
                    ]
                });

                Uni.store.PortalItems.add(
                    calendarItem
                );
            } else {
                var menuItem = Ext.create('Uni.model.MenuItem', {
                    text: Uni.I18n.translate('general.workspace', 'WSS', 'Workspace'),
                    portal: 'workspace',
                    glyph: 'settings'
                });

                Uni.store.MenuItems.add(menuItem);

                var calendarItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.webServices', 'WSS', 'Web services'),
                    portal: 'workspace',
                    items: [
                        {
                            text: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Web service endpoints'),
                            href: '#/workspace/webserviceendpoints',
                            hidden: !(Uni.Auth.hasPrivilege('privilege.view.webservices')),
                            route: 'webserviceendpoints'
                        },
                        {
                            text: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service history'),
                            href: '#/workspace/webservicehistory',
                            hidden: !(Uni.Auth.hasPrivilege('privilege.view.webservices')),
                            route: 'webservicehistory'
                        }
                    ]
                });

                Uni.store.PortalItems.add(
                    calendarItem
                );
            }
        }
    }
});