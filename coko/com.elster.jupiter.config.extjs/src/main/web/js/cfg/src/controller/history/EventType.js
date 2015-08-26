Ext.define('Cfg.controller.history.EventType', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration','CFG','Administration'),
            route: 'administration',
            disabled: true,
            items: {
                eventtypes: {
                    disabled: true,
                    title: 'Event types',
                    route: 'validation/eventtypes',
                    controller: 'Cfg.controller.EventType',
                    action: 'showOverview'
                }
            }
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
