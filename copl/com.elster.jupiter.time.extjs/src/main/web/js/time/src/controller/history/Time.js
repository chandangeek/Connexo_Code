Ext.define('Tme.controller.history.Time', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                relativeperiods: {
                    title: Uni.I18n.translate('general.relativeperiods', 'TME', 'Relative periods'),
                    route: 'relativeperiods',
                    controller: 'Tme.controller.RelativePeriods',
                    privileges: ['privilege.administrate.period','privilege.view.period'],
                    action: 'showRelativePeriods',
                    items: {
                        add: {
                            title: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
                            route: 'add',
                            controller: 'Tme.controller.RelativePeriods',
                            privileges: ['privilege.administrate.period'],
                            action: 'showAddRelativePeriod'
                        },
                        relativeperiod: {
                            title: Uni.I18n.translate('general.relativePeriod', 'TME', 'Relative period'),
                            route: '{periodId}',
                            controller: 'Tme.controller.RelativePeriods',
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
