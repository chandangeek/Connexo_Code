Ext.define('Cal.controller.history.Calendar', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'CAL', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                timeofuse: {
                    title: Uni.I18n.translate('general.timeOfUse', 'CAL', 'Time of use'),
                    // privileges: Sct.privileges.ServiceCallType.view,
                    route: 'timeofuse',
                    controller: 'Cal.controller.Calendars',
                    action: 'showTimeOfUseOverview'
                }
            }
        }
    }
});
