Ext.define('Scs.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Scs.controller.history.ServiceCall'
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
        //if (Scs.privileges.ServiceCall.canView() ){

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'SCS', 'Administration'),
                portal: 'administration',
                glyph: 'settings'
            });

            Uni.store.MenuItems.add(menuItem);

            var serviceCallItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                        href: '#/administration/servicecalls',
                        //hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.appServer') &&  Uni.Auth.hasNoPrivilege('privilege.view.appServer'),
                        route: 'servicecalls'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                serviceCallItem
            );
        }
    //}
});