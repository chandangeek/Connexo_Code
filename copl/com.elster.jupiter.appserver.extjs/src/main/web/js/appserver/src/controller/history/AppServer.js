Ext.define('Apr.controller.history.AppServer', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                appservers: {
                    title: Uni.I18n.translate('general.applicationServers', 'APR', 'Application servers'),
                    route: 'appservers',
                    controller: 'Apr.controller.AppServers',
                    action: 'showAppServers'
                }
            }
        }
    }
});
