Ext.define('Cal.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Cal.controller.history.Calendar',
        'Cal.controller.Calendars'
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
        var historian = this.getController('Cal.controller.history.Calendar');
    },

    initMenu: function () {
        //if (Scs.privileges.ServiceCall.canView() ){

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'CAL', 'Administration'),
            portal: 'administration',
            glyph: 'settings'
        });

        Uni.store.MenuItems.add(menuItem);

        var calendarItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.calendars', 'CAL', 'Calendars'),
            portal: 'administration',
            items: [
                {
                    text: Uni.I18n.translate('general.timeOfUseCalendars', 'CAL', 'Time of use calendars'),
                    href: '#/administration/timeofuse',
                    //hidden: !(Uni.Auth.hasPrivilege('privilege.view.serviceCalls') || Uni.Auth.hasPrivilege('privilege.administrate.serviceCall')),
                    route: 'timeofuse'
                }
            ]
        });

        Uni.store.PortalItems.add(
            calendarItem
        );
    }
    //}
});