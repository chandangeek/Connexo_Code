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
                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                'privilege.view.fineTuneValidationConfiguration.onDevice','privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
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
                            privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                        'privilege.view.fineTuneValidationConfiguration.onDevice','privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
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
                                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                        'privilege.view.fineTuneValidationConfiguration.onDevice', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
                                    action: 'showRules'
                                },
                                deviceconfigurations: {
                                    title: 'Device configurations',
                                    route: 'deviceconfigurations',
                                    controller: 'Mdc.controller.setup.RuleDeviceConfigurations',
                                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                        'privilege.view.fineTuneValidationConfiguration.onDevice', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
                                    action: 'showDeviceConfigView',
                                    items: {
                                        add: {
                                            title: 'Add',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.RuleDeviceConfigurations',
                                            privileges: ['privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
                                            action: 'showAddDeviceConfigView'
                                        }
                                    }
                                },
                                versions: {
                                    title: 'Versions',
                                    route: 'versions',
                                    controller: 'Cfg.controller.Validation',
                                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                        'privilege.view.fineTuneValidationConfiguration.onDevice', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
                                    action: 'showVersions',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('validation.addVersion', 'CFG', 'Add version'),
                                            route: 'add',
                                            controller: 'Cfg.controller.Validation',
                                            privileges: ['privilege.administrate.validationConfiguration'],
                                            action: 'addVersion'
                                        },
                                        overview: {
                                            title: 'Overview',
                                            route: '{versionId}',
                                            controller: 'Cfg.controller.Validation',
                                            privileges: ['privilege.administrate.validationConfiguration'],
                                            action: 'showVersionOverview',
                                            callback: function (route) {
                                                this.getApplication().on('loadVersion', function (record) {
                                                    route.setTitle(record.get('name'));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            },
                                            items: {
                                                edit: {
                                                    title: Uni.I18n.translate('validation.editVersion', 'CFG', 'Edit version'),
                                                    route: 'edit',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: ['privilege.administrate.validationConfiguration'],
                                                    action: 'editVersion'
                                                },
                                                clone: {
                                                    title: Uni.I18n.translate('validation.cloneVersion', 'CFG', 'Clone version'),
                                                    route: 'clone',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: ['privilege.administrate.validationConfiguration'],
                                                    action: 'cloneVersion'
                                                },
                                                rules: {
                                                    title: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
                                                    route: 'rules',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: ['privilege.administrate.validationConfiguration'],
                                                    action: 'showVersionRules',
                                                    items: {
                                                        add: {
                                                            title: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                                                            route: 'add',
                                                            controller: 'Cfg.controller.Validation',
                                                            privileges: ['privilege.administrate.validationConfiguration'],
                                                            action: 'addRule',
                                                            items: {
                                                                readingtypes: {
                                                                    title: 'Add reading types',
                                                                    route: 'readingtypes',
                                                                    controller: 'Cfg.controller.Validation',
                                                                    privileges: ['privilege.administrate.validationConfiguration'],
                                                                    action: 'addReadingTypes'
                                                                }
                                                            }
                                                        },
                                                        overview: {
                                                            title: 'Overview',
                                                            route: '{ruleId}',
                                                            controller: 'Cfg.controller.Validation',
                                                            privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                                                'privilege.view.fineTuneValidationConfiguration.onDevice', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
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
                                                                    action: 'showEditRuleOverview',
                                                                    items: {
                                                                        readingtypes: {
                                                                            title: 'Add reading types',
                                                                            route: 'readingtypes',
                                                                            controller: 'Cfg.controller.Validation',
                                                                            privileges: ['privilege.administrate.validationConfiguration'],
                                                                            action: 'addReadingTypes'
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