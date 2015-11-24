Ext.define('Mtr.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',
    requires:[],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'MTR', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                readingtypes: {
                    title: Uni.I18n.translate('general.readingTypes', 'MTR', 'Reading types'),
                    route: 'readingtypes',
                    controller: 'Mtr.readingtypes.controller.ReadingTypes',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.add', 'MTR', 'Add'),
                            route: 'add',
                            controller: 'Mtr.readingtypes.controller.AddReadingTypes',
                            privileges : Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        bulk: {
                            title: Uni.I18n.translate('general.readingTypes', 'MTR', 'Bulk action'),
                            route: 'bulk',
                            controller: 'Mtr.readingtypes.controller.BulkAction',
                            privileges : Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        }
                    }
                }
            }
        }
    }
});
