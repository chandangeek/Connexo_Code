/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Pkj.controller.history.Main',
        'Pkj.controller.TrustStores'
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
        var historian = this.getController('Pkj.controller.history.Main');
    },

    initMenu: function () {
        if (Pkj.privileges.TrustStore.canView() ){

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'PKJ', 'Administration'),
                portal: 'administration',
                glyph: 'settings'
            });

            Uni.store.MenuItems.add(menuItem);

            var certificateManagementItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.certificateManagement', 'PKJ', 'Certificate management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.trustStores', 'PKJ', 'Trust stores'),
                        href: '#/administration/truststores'
                        //hidden: Uni.Auth.hasNoPrivilege('privilege.view.trustStores'),
                        //route: 'truststores'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                certificateManagementItem
            );
        }
    }
});