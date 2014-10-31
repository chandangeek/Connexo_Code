Ext.define('Tme.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Uni.store.PortalItems',
        'Tme.controller.history.Time'
    ],

    controllers: [
        'Tme.controller.RelativePeriods',
        'Tme.controller.history.Time'
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
        var historian = this.getController('Tme.controller.history.Time');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            portal: 'administration',
            glyph: 'settings',
            index: 10
        });

        //if (Uni.Auth.hasAnyPrivilege(['privilege.upload.license', 'privilege.view.license'])) {
/*            var relativePeriodItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.licenses', 'TME', 'Relative Period'),
                portal: 'administration',
                route: 'relativeperiods',
                items: [
                    {
                        text: Uni.I18n.translate('general.licenses', 'TME', 'Relative Periods'),
                        href: '#/administration/relativeperiods/add',
                        route: 'licenses'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                relativePeriodItem
            );*/
        //}

        //Uni.store.MenuItems.add(menuItem);
    }
});

