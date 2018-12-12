/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Sam.controller.datapurge.Log',
        'Sam.controller.history.About',
        'Sam.controller.about.About',
        'Sam.controller.componentslist.ComponentsList',
        'Sam.controller.systeminfo.SystemInfo'
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
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Sam.controller.history.About'); // force route registration

        if (Sam.privileges.License.canView() || Sam.privileges.DataPurge.canView() || Sam.privileges.DeploymentInfo.canView()) {
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

            if (Sam.privileges.DeploymentInfo.canView()) {
                var deploymentInfoItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.deploymentInfo', 'SAM', 'Deployment information'),
                    portal: 'administration',
                    items: [
                        {
                            text: Uni.I18n.translate('general.componentsList', 'SAM', 'Components list'),
                            href: router.getRoute('administration/componentslist').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('general.systemInfo', 'SAM', 'System information'),
                            href: router.getRoute('administration/systeminfo').buildUrl()
                        }
                    ]
                });
                Uni.store.PortalItems.add(deploymentInfoItem);
            }
        }
    }
});

