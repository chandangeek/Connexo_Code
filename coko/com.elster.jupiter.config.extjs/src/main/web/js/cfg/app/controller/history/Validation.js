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
                        rules: {
                            title: 'Rules',
                            route: 'rules/{ruleSetId}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRules',
                            callback: function(route) {
                                this.getApplication().on('loadRuleSet', function(record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                edit: {
                                    title: 'Edit validation rule',
                                    route: 'edit/{ruleId}',
                                    controller: 'Cfg.controller.Validation',
                                    action: 'showEditRuleOverview'
                                }
                            }
                        },
                        overview: {
                            title: 'Overview',
                            route: 'overview/{id}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRuleSetOverview'
                        },
                        createset: {
                            title: 'Add validation rule set',
                            route: 'createset',
                            controller: 'Cfg.controller.Validation',
                            action: 'createEditRuleSet'
                        },
                        addRule: {
                            title: 'Add rule set',
                            route: 'addRule/{id}',
                            controller: 'Cfg.controller.Validation',
                            action: 'addRule'
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