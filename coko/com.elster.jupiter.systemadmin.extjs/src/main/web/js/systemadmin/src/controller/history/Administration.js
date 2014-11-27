Ext.define('Sam.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                licensing: {
                    title: 'Licensing',
                    route: 'licensing',
                    controller: 'Sam.controller.licensing.Licenses',
                    action: 'showOverview',
                    privileges: ['privilege.view.license', 'privilege.upload.license'],
                    items: {
                        licences: {
                            title: 'Licenses',
                            route: 'licenses',
                            controller: 'Sam.controller.licensing.Licenses',
                            privileges: ['privilege.view.license', 'privilege.upload.license'],
                            action: 'showOverview'
                        },
                        upload: {
                            title: 'Upload',
                            route: 'upload',
                            controller: 'Sam.controller.licensing.Upload',
                            privileges: ['privilege.upload.license'],
                            action: 'showOverview'
                        }
                    }
                },
                datapurgesettings: {
                    title: Uni.I18n.translate('datapurge.settings.title', 'SAM', 'Data purge settings'),
                    route: 'datapurgesettings',
                    controller: 'Sam.controller.datapurge.Settings',
                    action: 'showOverview'
                },
                datapurgehistory: {
                    title: Uni.I18n.translate('datapurge.history.breadcrumb', 'SAM', 'Data purge history'),
                    route: 'datapurgehistory',
                    controller: 'Sam.controller.datapurge.History',
                    action: 'showOverview',
                    items: {
                        log: {
                            title: Uni.I18n.translate('datapurge.log.title', 'SAM', 'Data purge log'),
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
