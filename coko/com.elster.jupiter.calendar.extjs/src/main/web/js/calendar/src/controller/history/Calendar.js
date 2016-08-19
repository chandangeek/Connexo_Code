Ext.define('Cal.controller.history.Calendar', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'CAL', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                timeofusecalendars: {
                    title: Uni.I18n.translate('general.timeOfUseCalendars', 'CAL', 'Time of use calendars'),
                    privileges: Cal.privileges.Calendar.admin,
                    route: 'timeofusecalendars',
                    controller: 'Cal.controller.Calendars',
                    action: 'showTimeOfUseOverview',
                    items: {
                        preview: {
                            title: Uni.I18n.translate('general.previewTOUCalendar', 'CAL', 'Preview time of use calendar'),
                            route: '{id}',
                            controller: 'Cal.controller.Calendars',
                            action: 'viewPreviewOfCalendar',
                            privileges: Cal.privileges.Calendar.admin,
                            callback: function (route) {
                                this.getApplication().on('timeofusecalendarloaded', function (name) {
                                    route.setTitle(Uni.I18n.translate('general.previewX', 'CAL', "Preview '{0}'", name, false));
                                }, {single: true});
                                return this;
                            }
                        }
                    }
                }
            }
        }
    }
});
