/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
    ],

    controllers: [
        'Sct.controller.history.ServiceCallType',
        'Sct.controller.ServiceCallTypes'
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
        var historian = this.getController('Sct.controller.history.ServiceCallType');
    },

    initMenu: function () {
        if (Sct.privileges.ServiceCallType.canView() ){

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'SCT', 'Administration'),
                portal: 'administration',
                glyph: 'settings'
            });

            Uni.store.MenuItems.add(menuItem);

            var serviceCallItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.serviceCalls', 'SCT', 'Service calls'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.serviceCallTypes', 'SCT', 'Service call types'),
                        href: '#/administration/servicecalltypes',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.view.serviceCallTypes'),
                        route: 'servicecalltypes'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                serviceCallItem
            );
        }
    }
});