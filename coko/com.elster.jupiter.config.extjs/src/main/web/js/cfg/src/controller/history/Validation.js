Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Cfg.privileges.Validation'
    ],

    rootToken: 'administration',
    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration','CFG','Administration'),
            route: 'administration',
            disabled: true,
            items: {
                rulesets: {
                    title: Uni.I18n.translate('general.validationRuleSets','CFG','Validation rule sets'),
                    route: 'validation/rulesets',
                    action: 'showRuleSets',
                    controller: 'Cfg.controller.Validation',
                    privileges: Cfg.privileges.Validation.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                            route: 'add',
                            controller: 'Cfg.controller.Validation',
                            privileges: Cfg.privileges.Validation.admin,
                            action: 'createEditRuleSet'
                        },
                        overview: {
                            title: Uni.I18n.translate('general.overview','CFG','Overview'),
                            route: '{ruleSetId}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRuleSetOverview',
                            privileges: Cfg.privileges.Validation.view,
                            callback: function (route) {
                                this.getApplication().on('loadRuleSet', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit','CFG','Edit'),
                                    route: 'edit',
                                    controller: 'Cfg.controller.Validation',
                                    privileges: Cfg.privileges.Validation.admin,
                                    action: 'createEditRuleSet'
                                },
                                deviceconfigurations: {
                                    title: Uni.I18n.translate('general.deviceConfigurations','CFG','Device configurations'),
                                    route: 'deviceconfigurations',
                                    controller: 'Mdc.controller.setup.RuleDeviceConfigurations',
                                    privileges: Cfg.privileges.Validation.view,
                                    action: 'showDeviceConfigView',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.add','CFG','Add'),
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.RuleDeviceConfigurations',
                                            privileges: Cfg.privileges.Validation.deviceConfiguration,
                                            action: 'showAddDeviceConfigView'
                                        }
                                    }
                                },
                                versions: {
                                    title: Uni.I18n.translate('general.versions','CFG','Versions'),
                                    route: 'versions',
                                    controller: 'Cfg.controller.Validation',
                                    privileges: Cfg.privileges.Validation.view,
                                    action: 'showVersions',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('validation.addVersion', 'CFG', 'Add version'),
                                            route: 'add',
                                            controller: 'Cfg.controller.Validation',
                                            privileges: Cfg.privileges.Validation.admin,
                                            action: 'addVersion'
                                        },
                                        overview: {
                                            title: Uni.I18n.translate('general.overview','CFG','Overview'),
                                            route: '{versionId}',
                                            controller: 'Cfg.controller.Validation',
                                            privileges: Cfg.privileges.Validation.view,
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
                                                    title: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                                                    route: 'edit',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: Cfg.privileges.Validation.admin,
                                                    action: 'editVersion'
                                                },
                                                clone: {
                                                    title: Uni.I18n.translate('validation.cloneVersion', 'CFG', 'Clone version'),
                                                    route: 'clone',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: Cfg.privileges.Validation.admin,
                                                    action: 'cloneVersion'
                                                },
                                                rules: {
                                                    title: Uni.I18n.translate('general.validationRules', 'CFG', 'Validation rules'),
                                                    route: 'rules',
                                                    controller: 'Cfg.controller.Validation',
                                                    privileges: Cfg.privileges.Validation.view,
                                                    action: 'showVersionRules',
                                                    items: {
                                                        add: {
                                                            title: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                                                            route: 'add',
                                                            controller: 'Cfg.controller.Validation',
                                                            privileges: Cfg.privileges.Validation.admin,
                                                            action: 'addRule',
                                                            items: {
                                                                readingtypes: {
                                                                    title: Uni.I18n.translate('general.addReadingTypes','CFG','Add reading types'),
                                                                    route: 'readingtypes',
                                                                    controller: 'Cfg.controller.Validation',
                                                                    privileges: Cfg.privileges.Validation.admin,
                                                                    action: 'addReadingTypes'
                                                                }
                                                            }
                                                        },
                                                        overview: {
                                                            title: Uni.I18n.translate('general.overview','CFG','Overview'),
                                                            route: '{ruleId}',
                                                            controller: 'Cfg.controller.Validation',
                                                            privileges: Cfg.privileges.Validation.view,
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
                                                                    title: Uni.I18n.translate('general.edit','CFG','Edit'),
                                                                    route: 'edit',
                                                                    controller: 'Cfg.controller.Validation',
                                                                    privileges: Cfg.privileges.Validation.admin,
                                                                    action: 'showEditRuleOverview',
                                                                    items: {
                                                                        readingtypes: {
                                                                            title: Uni.I18n.translate('general.addReadingTypes','CFG','Add reading types'),
                                                                            route: 'readingtypes',
                                                                            controller: 'Cfg.controller.Validation',
                                                                            privileges: Cfg.privileges.Validation.admin,
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