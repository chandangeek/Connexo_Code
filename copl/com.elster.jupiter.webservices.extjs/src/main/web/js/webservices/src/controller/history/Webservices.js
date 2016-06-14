Ext.define('Wss.controller.history.Webservices', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'WSS', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                webserviceendpoints: {
                    title: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Webservice endpoints'),
                    //privileges: Cal.privileges.Calendar.admin,
                    route: 'webserviceendpoints',
                    controller: 'Wss.controller.Webservices',
                    action: 'showWebservicesOverview',
                    items: {

                    }
                }
            }
        }
    }
});
