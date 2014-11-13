Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                rulesets: {
                    title: 'Validation rule sets',
                    route: 'validation/rulesets',
                    action: 'showRuleSets',
                    controller: 'Cfg.controller.Validation',
                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
                    items: {
                        add: {
                            title: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                            route: 'add',
                            controller: 'Cfg.controller.Validation',
                            privileges: ['privilege.administrate.validationConfiguration'],
                            action: 'createEditRuleSet'
                        },
                        overview: {
                            title: 'Overview',
                            route: '{ruleSetId}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRuleSetOverview',
                            privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
                            callback: function (route) {
                                this.getApplication().on('loadRuleSet', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: 'Edit',
                                    route: 'edit',
                                    controller: 'Cfg.controller.Validation',
                                    privileges: ['privilege.administrate.validationConfiguration'],
                                    action: 'createEditRuleSet'
                                },
                                rules: {
                                    title: 'Validation rules',
                                    route: 'rules',
                                    controller: 'Cfg.controller.Validation',
                                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
                                    action: 'showRules',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                                            route: 'add',
                                            controller: 'Cfg.controller.Validation',
                                            privileges: ['privilege.administrate.validationConfiguration'],
                                            action: 'addRule'
                                        },
                                        overview: {
                                            title: 'Overview',
                                            route: '{ruleId}',
                                            controller: 'Cfg.controller.Validation',
                                            privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
                                            action: 'showRuleOverview',
                                            callback: function (route) {
                                                this.getApplication().on('loadRule', function (record) {
                                                    route.setTitle(record.get('name'));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            },
                                            items: {
                                                edit: {
                                                    title: 'Edit',
                                                    route: 'edit',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: ['privilege.administrate.validationConfiguration'],
                                                    action: 'showEditRuleOverview'
                                                }
                                            }
                                        }
                                    }
                                },
                                deviceconfigurations: {
                                    title: 'Device configurations',
                                    route: 'deviceconfigurations',
                                    controller: 'Mdc.controller.setup.RuleDeviceConfigurations',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showDeviceConfigView',
                                    items: {
                                        add: {
                                            title: 'Add',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.RuleDeviceConfigurations',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                            action: 'showAddDeviceConfigView'
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