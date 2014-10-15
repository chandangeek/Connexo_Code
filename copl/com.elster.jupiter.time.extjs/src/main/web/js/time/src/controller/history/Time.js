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
                    action: 'showOverview',
                    privileges: [], // TODO
                    disabled: true, // TODO Enable when there is an actual overview page.
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                            route: 'add',
                            controller: 'Tme.controller.RelativePeriods',
                            action: 'showAddRelativePeriod',
                            privileges: [] // TODO
                        }
                    }
                }
            }
        }
    }
});
