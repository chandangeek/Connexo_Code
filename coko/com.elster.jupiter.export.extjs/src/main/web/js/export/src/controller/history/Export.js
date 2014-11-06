Ext.define('Dxp.controller.history.Export', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                dataexporttasks: {
                    title: Uni.I18n.translate('general.dataexporttasks', 'DES', 'Data export tasks'),
                    route: 'dataexporttasks',
                    controller: 'Dxp.controller.Tasks',
                    action: 'showDataExportTasks',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                            route: 'add',
                            controller: 'Dxp.controller.Tasks',
                            action: 'showAddExportTask'
                        },
                        dataexporttask: {
                            title: Uni.I18n.translate('general.dataexporttasks', 'DES', 'Data export task'),
                            route: '{taskId}',
                            controller: 'Dxp.controller.Tasks',
                            action: 'showTaskDetailsView',
                            callback: function (route) {
                                this.getApplication().on('dataexporttaskload', function (record) {
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
