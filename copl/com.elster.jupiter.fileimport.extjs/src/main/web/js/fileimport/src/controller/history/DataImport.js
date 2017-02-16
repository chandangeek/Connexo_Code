/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.controller.history.DataImport', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Fim.privileges.DataImport'
    ],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'FIM', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                importservices: {
                    title: Uni.I18n.translate('general.importServices', 'FIM', 'Import services'),
                    route: 'importservices',
                    controller: 'Fim.controller.ImportServices',
                    privileges: Fim.privileges.DataImport.view,
                    action: 'showImportServices',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addImportService', 'FIM', 'Add import service'),
                            route: 'add',
                            controller: 'Fim.controller.ImportServices',
                            privileges: Fim.privileges.DataImport.getAdmin,
                            action: 'showAddImportService'
                        },
                        importservice: {
                            title: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
                            route: '{importServiceId}',
                            controller: 'Fim.controller.ImportServices',
                            privileges: Fim.privileges.DataImport.view,
                            action: 'showImportService',
                            callback: function (route) {
                                this.getApplication().on('importserviceload', function (name) {
                                    route.setTitle(name);
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'FIM', 'Edit'),
                                    route: 'edit',
                                    controller: 'Fim.controller.ImportServices',
                                    privileges: Fim.privileges.DataImport.getAdmin,
                                    action: 'showEditImportService'
                                },
                                history: {
                                    title: Uni.I18n.translate('general.history', 'FIM', 'History'),
                                    route: 'history',
                                    controller: 'Fim.controller.History',
                                    action: 'showImportServicesHistory',
                                    privileges: Fim.privileges.DataImport.view,
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('general.log', 'FIM', 'Log'),
                                            route: '{occurrenceId}',
                                            controller: 'Fim.controller.Log',
                                            action: 'showImportServicesHistoryLog',
                                            filter: 'Fim.model.LogFilter',
                                            privileges: Fim.privileges.DataImport.view
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                importhistory: {
                    title: Uni.I18n.translate('general.importHistory', 'FIM', 'Import history'),
                    route: 'importhistory',
                    controller: 'Fim.controller.History',
                    action: 'showImportServicesHistory',
                    privileges: Fim.privileges.DataImport.view
                }
            }
        },
        workspace: {
            title: Uni.I18n.translate('title.workspace','FIM','Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                importhistory: {
                    title: Uni.I18n.translate('general.importHistory', 'FIM', 'Import history'),
                    route: 'importhistory',
                    controller: 'Fim.controller.History',
                    action: 'showImportServicesHistoryWorkspace',
                    privileges: Fim.privileges.DataImport.viewHistory,
                    items: {
                        occurrence: {
                            title: Uni.I18n.translate('general.log', 'FIM', 'Log'),
                            route: '{occurrenceId}',
                            controller: 'Fim.controller.Log',
                            action: 'showImportServicesHistoryLogWorkspace',
                            filter: 'Fim.model.LogFilter',
                            privileges: Fim.privileges.DataImport.viewHistory
                        }
                    }
                }
            }
        }
    }
});
