Ext.define('Cfg.controller.history.ValidationTask', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('dataValidationTasks.general.administration', 'UNI', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                datavalidationtasks: {
                    title: Uni.I18n.translate('dataValidationTasks.general.dataValidationTasks', 'CFG', 'Data validation tasks'),
                    route: 'datavalidationtasks',
                    controller: 'Cfg.controller.Tasks',
                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
                    action: 'showDataValidationTasks',
                    items: {
                        add: {
                            title: Uni.I18n.translate('dataValidationTasks.general.addDataValidationTask', 'CFG', 'Add data validation task'),
                            route: 'add',
                            controller: 'Cfg.controller.Tasks',
                            privileges: ['privilege.administrate.validationConfiguration'],
                            action: 'showAddValidationTask'
                        },
                        datavalidationtask: {
                            title: Uni.I18n.translate('dataValidationTasks.general.dataValidationTask', 'CFG', 'Data validation task'),
                            route: '{taskId}',
                            controller: 'Cfg.controller.Tasks',
                            action: 'showTaskDetailsView',
                            callback: function (route) {
                                this.getApplication().on('datavalidationtaskload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('dataValidationTasks.general.edit', 'CFG', 'Edit'),
                                    route: 'edit',
                                    controller: 'Cfg.controller.Tasks',
                                    privileges: ['privilege.administrate.validationConfiguration'],
                                    action: 'showEditValidationTask'
                                },
                                history: {
                                    title: Uni.I18n.translate('dataValidationTasks.general.dataValidationTaskHistory', 'CFG', 'Data validation task history'),
                                    route: 'history',
                                    controller: 'Cfg.controller.Tasks',
                                    action: 'showDataValidationTaskHistory',
                                    filter: 'Cfg.model.HistoryFilter',
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('dataValidationTasks.general.dataValidationTaskLog', 'CFG', 'Data validation task log'),
                                            route: '{occurrenceId}',
                                            controller: 'Cfg.controller.Log',
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
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
