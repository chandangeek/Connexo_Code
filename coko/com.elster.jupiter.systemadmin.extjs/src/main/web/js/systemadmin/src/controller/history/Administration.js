/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration','SAM','Administration'),
            route: 'administration',
            disabled: true,
            items: {
                licenses: {
                    title: Uni.I18n.translate('general.licenses','SAM','Licenses'),
                    route: 'licenses',
                    controller: 'Sam.controller.licensing.Licenses',
                    action: 'showOverview',
                    privileges: Sam.privileges.License.view,
                    items: {
                        upload: {
                            title: Uni.I18n.translate('general.upload','SAM','Upload'),
                            route: 'upload',
                            controller: 'Sam.controller.licensing.Upload',
                            privileges: Sam.privileges.License.upload,
                            action: 'showOverview'
                        }
                    }
                },
                datapurgesettings: {
                    title: Uni.I18n.translate('datapurge.settings.title', 'SAM', 'Data purge settings'),
                    route: 'datapurgesettings',
                    privileges: Sam.privileges.DataPurge.admin,
                    controller: 'Sam.controller.datapurge.Settings',
                    action: 'showOverview'
                },
                datapurgehistory: {
                    title: Uni.I18n.translate('datapurge.history.breadcrumb', 'SAM', 'Data purge history'),
                    route: 'datapurgehistory',
                    privileges: Sam.privileges.DataPurge.view,
                    controller: 'Sam.controller.datapurge.History',
                    action: 'showOverview',
                    items: {
                        log: {
                            title: Uni.I18n.translate('datapurge.log.breadcrumb', 'SAM', 'Data purge log'),
                            route: '{historyId}/log',
                            controller: 'Sam.controller.datapurge.Log',
                            action: 'showOverview'
                        }
                    }
                },
                systeminfo: {
                    title: Uni.I18n.translate('general.systemInfo', 'SAM', 'System information'),
                    route: 'systeminfo',
                    privileges: Sam.privileges.DeploymentInfo.view,
                    controller: 'Sam.controller.systeminfo.SystemInfo',
                    action: 'showSystemInfo'
                },
                componentslist: {
                    title: Uni.I18n.translate('general.componentsList', 'SAM', 'Components list'),
                    route: 'componentslist',
                    privileges: Sam.privileges.DeploymentInfo.view,
                    controller: 'Sam.controller.componentslist.ComponentsList',
                    action: 'showComponentsList'
                }
            }
        }
    }
});
