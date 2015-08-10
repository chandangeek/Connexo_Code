Ext.define('Sam.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.history.EventBus'
    ],

    controllers: [
        'Sam.controller.history.Administration',
        'Sam.controller.licensing.Licenses',
        'Sam.controller.licensing.Upload',
        'Sam.controller.datapurge.Settings',
        'Sam.controller.datapurge.History',
        'Sam.controller.datapurge.Log'
    ],

    stores: [
        'Sam.store.Licensing'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (Sam.privileges.License.canView() || Sam.privileges.DataPurge.canView() ) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'SAM', 'Administration'),
                href: me.getController('Sam.controller.history.Administration').tokenizeShowOverview(),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });
            Uni.store.MenuItems.add(menuItem);

            if (Sam.privileges.License.canView()) {
                var licensingItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.licensing', 'SAM', 'Licensing'),
                    portal: 'administration',
                    route: 'licensing',
                    items: [
                        {
                            text: Uni.I18n.translate('general.licenses', 'SAM', 'Licenses'),
                            href: router.getRoute('administration/licenses').buildUrl(),
                            route: 'licenses'
                        }
                    ]
                });

                Uni.store.PortalItems.add(licensingItem);
            }

            if (Sam.privileges.DataPurge.canView()) {
                var dataPurgeItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('datapurge.title', 'SAM', 'Data purge'),
                    portal: 'administration',
                    items: [
                        {
                            text: Uni.I18n.translate('datapurge.settings.title', 'SAM', 'Data purge settings'),
                            privileges: Sam.privileges.DataPurge.admin,
                            href: typeof router.getRoute('administration/datapurgesettings') !== 'undefined'
                                ? router.getRoute('administration/datapurgesettings').buildUrl() : ''
                        },
                        {
                            text: Uni.I18n.translate('datapurge.history.breadcrumb', 'SAM', 'Data purge history'),
                            href: router.getRoute('administration/datapurgehistory').buildUrl()
                        }
                    ]
                });
                Uni.store.PortalItems.add(dataPurgeItem);
            }
        }
    }
});

