Ext.define('Cps.main.controller.History', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'EST', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                customattributesets: {
                    title: Uni.I18n.translate('general.customAttributeSets', 'CPS', 'Custom attribute sets'),
                    route: 'customattributesets',
                    privileges: Cps.privileges.CustomAttributeSets.view,
                    controller: 'Cps.customattributesets.controller.AttributeSets',
                    action: 'showCustomAttributeSets'
                }
            }
        }
    }
});
