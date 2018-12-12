/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Scs.controller.history.ServiceCall',
        'Scs.controller.ServiceCalls'
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
        var historian = this.getController('Scs.controller.history.ServiceCall');
    },

    initMenu: function () {
        if (Scs.privileges.ServiceCall.canView() ){

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'SCS', 'Workspace'),
                portal: 'workspace',
                glyph: 'workspace',
                index: 30
            });

            Uni.store.MenuItems.add(menuItem);

            var serviceCallItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                portal: 'workspace',
                items: [
                    {
                        text: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                        href: '#/workspace/servicecalls',
                        hidden: !(Uni.Auth.hasPrivilege('privilege.view.serviceCalls') || Uni.Auth.hasPrivilege('privilege.administrate.serviceCall')),
                        route: 'servicecalls'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                serviceCallItem
            );
        }
    }
});