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
                            route: 'rules/{id}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRules'
                        },
                        overview: {
                            title: 'Overview',
                            route: 'overview/{id}',
                            controller: 'Cfg.controller.Validation',
                            action: 'showRuleSetOverview'
                        },
                        createset: {
                            title: 'Add rule set',
                            route: 'createset',
                            controller: 'Cfg.controller.Validation',
                            action: 'newRuleSet'
                        },
                        addRule: {
                            title: 'Add rule set',
                            route: 'addRule/{id}',
                            controller: 'Cfg.controller.Validation',
                            action: 'addRule'
                        }
                    }
                }
            }
        }
    }
});