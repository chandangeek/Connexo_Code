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
        //if (Cal.privileges.Calendar.canAdministrate()) {

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'WSS', 'Administration'),
                portal: 'administration',
                glyph: 'settings'
            });

            Uni.store.MenuItems.add(menuItem);

            var calendarItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.webservices', 'WSS', 'Webservices'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Webservice endpoints'),
                        href: '#/administration/webserviceendpoints',
                        //hidden: !(Uni.Auth.hasPrivilege('privilege.administrate.touCalendars')),
                        route: 'webserviceendpoints'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                calendarItem
            );
        }
    //}
});