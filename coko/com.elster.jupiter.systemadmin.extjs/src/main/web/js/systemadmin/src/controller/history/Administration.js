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
                    title: 'Licenses',
                    route: 'licenses',
                    controller: 'Sam.controller.licensing.Licenses',
                    action: 'showOverview',
                    privileges: Sam.privileges.License.view,
                    items: {
                        upload: {
                            title: 'Upload',
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
                }
            }
        }
    }
});
