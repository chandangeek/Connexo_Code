Ext.define('Dxp.controller.history.Export', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Dxp.privileges.DataExport'
    ],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'DES', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                dataexporttasks: {
                    title: Uni.I18n.translate('general.dataExportTasks', 'DES', 'Data export tasks'),
                    route: 'dataexporttasks',
                    controller: 'Dxp.controller.Tasks',
                    privileges : Dxp.privileges.DataExport.view,
                    action: 'showDataExportTasks',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add data export task'),
                            route: 'add',
                            controller: 'Dxp.controller.Tasks',
                            privileges: Dxp.privileges.DataExport.admin,
                            action: 'showAddExportTask',
                            items: {
                                readingtypes: {
                                    title: 'Add reading types',
                                    route: 'readingtypes',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: Dxp.privileges.DataExport.admin,
                                    action: 'addReadingTypes'
                                },
                                destination: {
                                    title: 'Add destination',
                                    route: 'destination',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: Dxp.privileges.DataExport.admin,
                                    action: 'addDestination'
                                }
                            }
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
                                    title: Uni.I18n.translate('general.edit', 'DES', 'Edit'),
                                    route: 'edit',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: Dxp.privileges.DataExport.update,
                                    action: 'showEditExportTask',
                                    items: {
                                        readingtypes: {
                                            title: 'Add reading types',
                                            route: 'readingtypes',
                                            controller: 'Dxp.controller.Tasks',
                                            privileges: Dxp.privileges.DataExport.update,
                                            action: 'addReadingTypes'
                                        },
                                        destination: {
                                            title: 'Add destination',
                                            route: 'destination',
                                            controller: 'Dxp.controller.Tasks',
                                            privileges: Dxp.privileges.DataExport.update,
                                            action: 'addDestination'
                                        }
                                    }
                                },
                                history: {
                                    title: Uni.I18n.translate('general.dataExportTaskHistory', 'DES', 'Data export task history'),
                                    route: 'history',
                                    controller: 'Dxp.controller.Tasks',
                                    action: 'showDataExportTaskHistory',
                                    filter: 'Dxp.model.HistoryFilter',
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('general.dataExportTaskLog', 'DES', 'Data export task log'),
                                            route: '{occurrenceId}',
                                            controller: 'Dxp.controller.Log',
                                            action: 'showLog'
                                        }
                                    }
                                },
                                datasources: {
                                    title: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
                                    route: 'datasources',
                                    controller: 'Dxp.controller.Tasks',
                                    action: 'showDataSources'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
