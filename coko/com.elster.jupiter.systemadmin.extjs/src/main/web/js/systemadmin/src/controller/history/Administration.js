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
                    items: {
                        licences: {
                            title: 'Licenses',
                            route: 'licenses',
                            controller: 'Sam.controller.licensing.Licenses',
                            action: 'showOverview'
                        },
                        upload: {
                            title: 'Upload',
                            route: 'upload',
                            controller: 'Sam.controller.licensing.Upload',
                            action: 'showOverview'
                        }
                    }
                }
            }
        }
    }
});
