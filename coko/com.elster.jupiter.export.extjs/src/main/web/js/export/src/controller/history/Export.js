Ext.define('Dxp.controller.history.Export', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                dataexporttasks: {
                    title: Uni.I18n.translate('general.dataexporttasks', 'DXP', 'Data export tasks'),
                    route: 'dataexporttasks',
                    controller: 'Dxp.controller.Tasks',
                    action: 'showOverview',
                    privileges: [], // TODO
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                            route: 'add',
                            controller: 'Dxp.controller.Tasks',
                            action: 'showAddExportTask',
                            privileges: [] // TODO
                        }
                    }
                }
            }
        }
    }
});
