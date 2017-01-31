/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Apr.controller.history.AppServer',
        'Apr.controller.AppServers',
        'Apr.controller.TaskOverview',
        'Apr.controller.MessageQueues'
    ],

    stores: [
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();

        this.callParent(arguments);
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Apr.controller.history.AppServer');
    },

    initMenu: function () {
        if (Apr.privileges.AppServer.canView() ){

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'APR', 'Administration'),
                portal: 'administration',
                glyph: 'settings'
            });

            Uni.store.MenuItems.add(menuItem);

            var appServerItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.applicationServer', 'APR', 'Application server'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.applicationServers', 'APR', 'Application servers'),
                        href: '#/administration/appservers',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.appServer') &&  Uni.Auth.hasNoPrivilege('privilege.view.appServer'),
                        route: 'appservers'
                    },
                    {
                        text: Uni.I18n.translate('general.taskOverview', 'APR', 'Task overview'),
                        href: '#/administration/taskoverview',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.view.ViewTaskOverview'),
                        route: 'taskoverview'
                    },
                    {
                        text: Uni.I18n.translate('general.messageQueues', 'APR', 'Message queues'),
                        href: '#/administration/messagequeues',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.appServer') &&  Uni.Auth.hasNoPrivilege('privilege.view.appServer'),
                        route: 'messagequeues'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                appServerItem
            );
        }
    }
});