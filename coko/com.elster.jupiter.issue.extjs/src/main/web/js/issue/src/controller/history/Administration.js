Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration : {
            title: Uni.I18n.translate('route.administration', 'ISU', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                assignmentrules: {
                    title: Uni.I18n.translate('route.assignmentRules', 'ISU', 'Assignment Rules'),
                    route: 'assignmentrules',
                    controller: 'Isu.controller.IssueAssignmentRules',
                    privileges: ['privilege.view.assignmentRule']
                },
                creationrules: {
                    title: Uni.I18n.translate('route.issueCreationRules', 'ISU', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.IssueCreationRules',
                    privileges: ['privilege.administrate.creationRule','privilege.view.creationRule'],
                    items: {
                        add: {
                            title: Uni.I18n.translate('route.addIssueCreationRule', 'ISU', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.IssueCreationRulesEdit',
                            privileges: ['privilege.administrate.creationRule'],
                            action: 'showCreate',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('route.addAction', 'ISU', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.IssueCreationRulesActionsEdit',
                                    privileges: ['privilege.administrate.creationRule'],
                                    action: 'showCreate'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Isu.controller.IssueCreationRulesEdit',
                            privileges: ['privilege.administrate.creationRule'],
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