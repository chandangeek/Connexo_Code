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
                    items: {
                        add: {
                            title: 'Add',
                            route: 'add',
                            controller: 'Cfg.controller.Validation',
                            action: 'createEditRuleSet'
                        },
                        overview: {
                            title: 'Overview',
                            route: '{ruleSetId}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRuleSetOverview',
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
                                    action: 'createEditRuleSet'
                                },
                                rules: {
                                    title: 'Validation rules',
                                    route: 'rules',
                                    controller: 'Cfg.controller.Validation',
                                    action: 'showRules',
                                    items: {
                                        add: {
                                            title: 'Add',
                                            route: 'add',
                                            controller: 'Cfg.controller.Validation',
                                            action: 'addRule'
                                        },
                                        overview: {
                                            title: 'Overview',
                                            route: '{ruleId}',
                                            controller: 'Cfg.controller.Validation',
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
                                                    action: 'showEditRuleOverview'
                                                }
                                            }
                                        }
                                    }
                                },
                                deviceconfigurations: {
                                    title: 'Device configurations',
                                    route: 'deviceconfigurations',
                                    controller: 'Cfg.controller.RuleDeviceConfigurations',
                                    action: 'showDeviceConfigView',
                                    items: {
                                        add: {
                                            title: 'Add',
                                            route: 'add',
                                            controller: 'Cfg.controller.RuleDeviceConfigurations',
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