Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration : {
            title: Uni.I18n.translate('route.administration', 'ISE', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                assignmentrules: {
                    title: Uni.I18n.translate('route.assignmentRules', 'ISE', 'Assignment Rules'),
                    route: 'assignmentrules',
                    controller: 'Isu.controller.IssueAssignmentRules'
                },
                creationrules: {
                    title: Uni.I18n.translate('route.issueCreationRules', 'ISE', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.IssueCreationRules',
                    items: {
                        add: {
                            title: Uni.I18n.translate('route.addIssueCreationRule', 'ISE', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.IssueCreationRulesEdit',
                            action: 'showCreate',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('route.addAction', 'ISE', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.IssueCreationRulesActionsEdit',
                                    action: 'showCreate'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Isu.controller.IssueCreationRulesEdit',
                            action: 'showEdit'
                        }
                    }
                }
            }
        }
    },

    init :function() {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});