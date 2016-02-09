Ext.define('Scs.controller.history.ServiceCall', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'SCS', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                servicecalls: {
                    title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                    route: 'servicecalls',
                    controller: 'Scs.controller.ServiceCalls',
                    action: 'showServiceCalls'
                }
            }
        }
    }
});
