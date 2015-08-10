Ext.define('Cfg.controller.history.ValidationTask', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Cfg.privileges.Validation'
    ],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('validationTasks.general.administration', 'CFG', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                validationtasks: {
                    title: Uni.I18n.translate('validationTasks.general.validationTasks', 'CFG', 'Validation tasks'),
                    route: 'validationtasks',
                    controller: 'Cfg.controller.Tasks',
                    privileges: Cfg.privileges.Validation.viewTasks,
                    action: 'showValidationTasks',
                    items: {
                        add: {
                            title: Uni.I18n.translate('validationTasks.general.addValidationTask', 'CFG', 'Add validation task'),
                            route: 'add',
                            controller: 'Cfg.controller.Tasks',
                            privileges: Cfg.privileges.Validation.admin,
                            action: 'showAddValidationTask'
                        },
                        validationtask: {
                            title: Uni.I18n.translate('validationTasks.general.validationTask', 'CFG', 'Validation task'),
                            route: '{taskId}',
                            controller: 'Cfg.controller.Tasks',
                            action: 'showValidationTaskDetailsView',
                            callback: function (route) {
                                this.getApplication().on('validationtaskload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('validationTasks.general.edit', 'CFG', 'Edit'),
                                    route: 'edit',
                                    controller: 'Cfg.controller.Tasks',
                                    privileges: Cfg.privileges.Validation.admin,
                                    action: 'showEditValidationTask'
                                },
                                history: {
                                    title: Uni.I18n.translate('validationTasks.general.validationTaskHistory', 'CFG', 'Validation task history'),
                                    route: 'history',
                                    controller: 'Cfg.controller.Tasks',
                                    action: 'showValidationTaskHistory',
                                    filter: 'Cfg.model.HistoryFilter',
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('validationTasks.general.validationTaskLog', 'CFG', 'Validation task log'),
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
