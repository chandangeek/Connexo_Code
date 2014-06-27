Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                validation: {
                    title: 'Validation rule sets',
                    route: 'validation',
                    controller: 'Cfg.controller.Validation',
                    action: 'showRuleSets',
                    items: {
                        rulesets: {
                            title: 'Rules',
                            route: 'rulesets',
                            controller: 'Cfg.controller.Validation',
                            callback: function (route) {
                                this.getApplication().on('loadRuleSet', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                overview: {
                                    title: 'Overview',
                                    route: 'overview/{ruleSetId}',
                                    controller: 'Cfg.controller.Validation',
                                    action: 'showRuleSetOverview'
                                },
                                validationrules: {
                                    title: 'Validation rules',
                                    route: 'validationrules/{ruleSetId}',
                                    controller: 'Cfg.controller.Validation',
                                    action: 'showRules',
                                    items: {
                                        overview: {
                                            title: 'Overview',
                                            route: 'overview/{ruleSetId}',
                                            controller: 'Cfg.controller.Validation',
                                            action: 'showRuleSetOverview'
                                        },
                                        edit: {
                                            title: 'Edit validation rule',
                                            route: 'edit/{ruleId}',
                                            controller: 'Cfg.controller.Validation',
                                            action: 'showEditRuleOverview'
                                        },
                                        addRule: {
                                            title: 'Add validation rule',
                                            route: 'addRule/{ruleSetId}',
                                            controller: 'Cfg.controller.Validation',
                                            action: 'addRule'
                                        },
                                        ruleoverview: {
                                            title: 'Overview',
                                            route: 'ruleoverview/{ruleId}',
                                            controller: 'Cfg.controller.Validation',
                                            action: 'showRuleOverview'
                                        }
                                    }
                                }
                            }
                        },
                        createset: {
                            title: 'Add validation rule set',
                            route: 'createset',
                            controller: 'Cfg.controller.Validation',
                            action: 'createEditRuleSet'
                        },
                        editset: {
                            title: 'Edit validation rule set',
                            route: 'editset/{ruleSetId}',
                            controller: 'Cfg.controller.Validation',
                            action: 'createEditRuleSet'
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