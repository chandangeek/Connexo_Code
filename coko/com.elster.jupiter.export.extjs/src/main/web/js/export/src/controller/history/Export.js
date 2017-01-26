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
                    title: Uni.I18n.translate('general.exportTasks', 'DES', 'Export tasks'),
                    route: 'dataexporttasks',
                    controller: 'Dxp.controller.Tasks',
                    privileges : Dxp.privileges.DataExport.view,
                    action: 'showDataExportTasks',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addExportTask', 'DES', 'Add export task'),
                            route: 'add',
                            controller: 'Dxp.controller.Tasks',
                            privileges: Dxp.privileges.DataExport.admin,
                            action: 'showAddExportTask',
                            items: {
                                readingtypes: {
                                    title: Uni.I18n.translate('general.addReadingTypes','DES','Add reading types'),
                                    route: 'readingtypes',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: Dxp.privileges.DataExport.admin,
                                    action: 'addReadingTypes'
                                },
                                destination: {
                                    title: Uni.I18n.translate('general.addDestination','DES','Add destination'),
                                    route: 'destination',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: Dxp.privileges.DataExport.admin,
                                    action: 'addDestination'
                                }
                            }
                        },
                        dataexporttask: {
                            title: Uni.I18n.translate('general.exportTask', 'DES', 'Export task'),
                            route: '{taskId}',
                            controller: 'Dxp.controller.Tasks',
                            privileges: Dxp.privileges.DataExport.view,
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
                                            title: Uni.I18n.translate('general.addReadingTypes','DES','Add reading types'),
                                            route: 'readingtypes',
                                            controller: 'Dxp.controller.Tasks',
                                            privileges: Dxp.privileges.DataExport.update,
                                            action: 'addReadingTypes'
                                        },
                                        destination: {
                                            title: Uni.I18n.translate('general.addDestination','DES','Add destination'),
                                            route: 'destination',
                                            controller: 'Dxp.controller.Tasks',
                                            privileges: Dxp.privileges.DataExport.update,
                                            action: 'addDestination'
                                        }
                                    }
                                },
                                history: {
                                    title: Uni.I18n.translate('general.exportTaskHistory', 'DES', 'Export task history'),
                                    route: 'history',
                                    controller: 'Dxp.controller.Tasks',
                                    action: 'showDataExportTaskHistory',
                                    filter: 'Dxp.model.HistoryFilter',
                                    privileges: Dxp.privileges.DataExport.view,
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('general.exportTaskLog', 'DES', 'Export task log'),
                                            route: '{occurrenceId}',
                                            controller: 'Dxp.controller.Log',
                                            privileges: Dxp.privileges.DataExport.view,
                                            action: 'showLog'
                                        }
                                    }
                                },
                                datasources: {
                                    title: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
                                    route: 'datasources',
                                    controller: 'Dxp.controller.Tasks',
                                    privileges: Dxp.privileges.DataExport.view,
                                    action: 'showDataSources'
                                }
                            }
                        }
                    }
                },
                exporthistory: {
                    title: Uni.I18n.translate('general.exportTasksHistory', 'DES', 'Export tasks history'),
                    route: 'exporthistory',
                    controller: 'Dxp.controller.Tasks',
                    action: 'showDataExportTaskHistory',
                    filter: 'Dxp.model.HistoryFilter',
                    privileges: Dxp.privileges.DataExport.view
                }
            }
        },
        workspace: {
            title: Uni.I18n.translate('title.workspace','DES','Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                exporthistory: {
                    title: Uni.I18n.translate('general.history', 'DES', 'History'),
                    route: 'exporthistory',
                    controller: 'Dxp.controller.Tasks',
                    action: 'showDataExportTaskHistory',
                    filter: 'Dxp.model.HistoryFilter',
                    privileges: Dxp.privileges.DataExport.view,
                    items: {
                        occurrence: {
                            title: Uni.I18n.translate('general.log', 'DES', 'Log'),
                            route: '{occurrenceId}',
                            controller: 'Dxp.controller.Log',
                            privileges: Dxp.privileges.DataExport.view,
                            action: 'showLog'
                        }
                    }
                }
            }
        }
    }
});
