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
                    title: Uni.I18n.translate('general.dataExportTasks', 'DES', 'Data export tasks'),
                    route: 'dataexporttasks',
                    controller: 'Dxp.controller.Tasks',
                    privileges: ['privilege.administrate.dataExportTask','privilege.view.dataExportTask','privilege.update.dataExportTask','privilege.update.schedule.dataExportTask','privilege.run.dataExportTask'],
                    action: 'showDataExportTasks',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add data export task'),
                            route: 'add',
                            controller: 'Dxp.controller.Tasks',
                            privileges: ['privilege.administrate.dataExportTask'],
                            action: 'showAddExportTask'
                        },
                        dataexporttask: {
                            title: Uni.I18n.translate('general.dataExportTask', 'DES', 'Data export task'),
                            route: '{taskId}',
                            controller: 'Dxp.controller.Tasks',
                            action: 'showTaskDetailsView',
                            callback: function (route) {
                                this.getApplication().on('dataexporttaskload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.editDataExportTask', 'DES', 'Edit data export task'),
                                    route: 'edit',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: ['privilege.update.dataExportTask','privilege.update.schedule.dataExportTask'],
                                    action: 'showEditExportTask'
                                },
                                history: {
                                    title: Uni.I18n.translate('general.dataExportTaskHistory', 'DES', 'Data export task history'),
                                    route: 'history',
                                    controller: 'Dxp.controller.Tasks',
                                    action: 'showDataExportTaskHistory',
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('general.dataExportTaskLog', 'DES', 'Data export task log'),
                                            route: '{occurrenceId}',
                                            controller: 'Dxp.controller.Log',
                                            action: 'showLog'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
