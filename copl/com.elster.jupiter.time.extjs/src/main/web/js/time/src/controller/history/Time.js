Ext.define('Tme.controller.history.Time', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration','TME','Administration'),
            route: 'administration',
            disabled: true,
            items: {
                relativeperiods: {
                    title: Uni.I18n.translate('general.relativePeriods', 'TME', 'Relative periods'),
                    route: 'relativeperiods',
                    controller: 'Tme.controller.RelativePeriods',
                    privileges: Tme.privileges.Period.view,
                    action: 'showRelativePeriods',
                    items: {
                        add: {
                            title: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
                            route: 'add',
                            controller: 'Tme.controller.RelativePeriods',
                            privileges: Tme.privileges.Period.admin,
                            action: 'showAddRelativePeriod'
                        },
                        relativeperiod: {
                            title: Uni.I18n.translate('general.relativePeriod', 'TME', 'Relative period'),
                            route: '{periodId}',
                            controller: 'Tme.controller.RelativePeriods',
                            privileges: Tme.privileges.Period.view,
                            action: 'showRelativePeriodDetails',
                            callback: function (route) {
                                this.getApplication().on('relativeperiodload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
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
