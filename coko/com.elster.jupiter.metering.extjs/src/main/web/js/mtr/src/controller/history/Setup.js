Ext.define('Mtr.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',
    requires:[],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'DES', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                readingtypes: {
                    title: Uni.I18n.translate('general.readingTypes', 'MTR', 'Reading types'),
                    route: 'readingtypes',
                    controller: 'Mtr.readingtypes.controller.View',
             //       privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {}
                }
            }
        }
    }
});
