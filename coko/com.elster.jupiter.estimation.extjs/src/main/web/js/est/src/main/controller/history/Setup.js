Ext.define('Est.main.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'EST', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                estimationrulesets: {
                    title: Uni.I18n.translate('estimationrulesets.estimationrulesets', 'EST', 'Estimation rule sets'),
                    route: 'estimationrulesets',
                    privileges: Est.privileges.EstimationConfiguration.view,
                    controller: 'Est.estimationrulesets.controller.EstimationRuleSets',
                    action: 'showEstimationRuleSets',
                    items: {
                        addruleset: {
                            title: Uni.I18n.translate('estimationrulesets.add.title', 'EST', 'Add estimation rule set'),
                            route: 'add',
                            privileges: Est.privileges.EstimationConfiguration.administrate,
                            controller: 'Est.estimationrulesets.controller.EstimationRuleSets',
                            action: 'showEstimationRuleSetAdd'
                        },
                        estimationruleset: {
                            title: Uni.I18n.translate('estimationrulesets.estimationrulesets', 'EST', 'Estimation rule sets'),
                            route: '{ruleSetId}',
                            privileges: Est.privileges.EstimationConfiguration.view,
                            controller: 'Est.estimationrulesets.controller.EstimationRuleSets',
                            action: 'showEstimationRuleSetDetails',
                            callback: function (route) {
                                this.getApplication().on('loadEstimationRuleSet', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
                                    route: 'edit',
                                    privileges: Est.privileges.EstimationConfiguration.administrate,
                                    controller: 'Est.estimationrulesets.controller.EstimationRuleSets',
                                    action: 'showEstimationRuleSetEdit'
                                },
                                rules: {
                                    title: Uni.I18n.translate('general.estimationRules', 'EST', 'Estimation rules'),
                                    route: 'rules',
                                    privileges: Est.privileges.EstimationConfiguration.view,
                                    controller: 'Est.estimationrules.controller.Overview',
                                    action: 'showOverview',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('estimationrules.addEstimationRule', 'EST', 'Add estimation rule'),
                                            route: 'add',
                                            privileges: Est.privileges.EstimationConfiguration.administrate,
                                            controller: 'Est.estimationrules.controller.Edit',
                                            action: 'showOverview',
                                            items: {
                                                addreadingtypes: {
                                                    title: Uni.I18n.translate('general.addReadingTypes', 'EST', 'Add reading types'),
                                                    route: 'addreadingtypes',
                                                    privileges: ['privilege.administrate.EstimationConfiguration'],
                                                    controller: 'Est.estimationrules.controller.AddReadingTypes',
                                                    action: 'showOverview'
                                                }
                                            }
                                        },
                                        rule: {
                                            title: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),
                                            route: '{ruleId}',
                                            privileges: Est.privileges.EstimationConfiguration.view,
                                            controller: 'Est.estimationrules.controller.Detail',
                                            action: 'showOverview',
                                            callback: function (route) {
                                                this.getApplication().on('loadEstimationRule', function (record) {
                                                    route.setTitle(record.get('name'));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            },
                                            items: {
                                                edit: {
                                                    title: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
                                                    route: 'edit',
                                                    privileges: Est.privileges.EstimationConfiguration.administrate,
                                                    controller: 'Est.estimationrules.controller.Edit',
                                                    action: 'showOverview',
                                                    items: {
                                                        addreadingtypes: {
                                                            title: Uni.I18n.translate('general.addReadingTypes', 'EST', 'Add reading types'),
                                                            route: 'addreadingtypes',
                                                            privileges: ['privilege.administrate.EstimationConfiguration'],
                                                            controller: 'Est.estimationrules.controller.AddReadingTypes',
                                                            action: 'showOverview'
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                deviceconfigurations: {
                                    title: Uni.I18n.translate('general.deviceConfigurations', 'EST', 'Device configurations'),
                                    route: 'deviceconfigurations',
                                    controller: 'Mdc.controller.setup.EstimationDeviceConfigurations',
                                    action: 'showEstimationDeviceConfigurations',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.addDeviceConfigurations', 'EST', 'Add device configurations'),
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.EstimationDeviceConfigurations',
                                            action: 'showAddEstimationDeviceConfigurations',
                                            privileges: Est.privileges.EstimationConfiguration.viewfineTuneEstimationConfiguration
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                estimationtasks: {
                    title: Uni.I18n.translate('estimationtasks.title', 'EST', 'Estimation tasks'),
                    route: 'estimationtasks',
                    controller: 'Est.estimationtasks.controller.EstimationTasksOverview',
                    action: 'showEstimationTasksOverview',
                    privileges: Est.privileges.EstimationConfiguration.viewTask,
                    items: {
                        add: {
                            title: Uni.I18n.translate('estimationtasks.general.addEstimationTask', 'EST', 'Add estimation task'),
                            route: 'add',
                            controller: 'Est.estimationtasks.controller.EstimationTasksAddEdit',
                            action: 'showAddEstimationTasksView',
                            privileges: Est.privileges.EstimationConfiguration.administrateTask
                        },
                        estimationtask: {
                            title: Uni.I18n.translate('general.estimationTask', 'EST', 'Estimation task'),
                            route: '{taskId}',
                            controller: 'Est.estimationtasks.controller.EstimationTasksDetails',
                            action: 'showEstimationTaskDetails',
                            privileges: Est.privileges.EstimationConfiguration.viewTask,
                            callback: function (route) {
                                this.getApplication().on('estimationTaskLoaded', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
                                    route: 'edit',
                                    controller: 'Est.estimationtasks.controller.EstimationTasksAddEdit',
                                    privileges: Est.privileges.EstimationConfiguration.updateTask,
                                    action: 'showEditEstimationTasksView'
                                },
                                history: {
                                    title: Uni.I18n.translate('estimationtasks.general.history', 'EST', 'History'),
                                    route: 'history',
                                    controller: 'Est.estimationtasks.controller.EstimationTasksHistory',
                                    action: 'showEstimationTaskHistory',
                                    filter: 'Est.estimationtasks.model.HistoryFilter',
                                    privileges: Est.privileges.EstimationConfiguration.viewTask,
                                    items: {
                                        occurrence: {
                                            title: Uni.I18n.translate('estimationtasks.general.estimationtaskLog', 'EST', 'Estimation task log'),
                                            route: '{occurrenceId}',
                                            controller: 'Est.estimationtasks.controller.EstimationTasksLog',
                                            action: 'showLog',
                                            privileges: Est.privileges.EstimationConfiguration.viewTask
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
