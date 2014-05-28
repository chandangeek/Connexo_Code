Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration : {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                issue: {
                    title: 'Issue',
                    route: 'issue',
                    items: {
                        assignmentrules: {
                            title: 'Assignment Rules',
                            route: 'assignmentrules',
                            controller: 'Isu.controller.IssueAssignmentRules'
                        },
                        creationrules: {
                            title: 'Creation Rules',
                            route: 'creationrules',
                            controller: 'Isu.controller.IssueCreationRules',
                            items: {
                                create: {
                                    title: 'Create',
                                    route: 'create',
                                    controller: 'Isu.controller.IssueCreationRulesEdit',
                                    action: 'showCreate'
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
                },
                communicationtasks: {
                    title: 'Communication Tasks',
                    route: 'communicationtasks',
                    controller: 'Isu.controller.CommunicationTasksView',
                    items: {
                        create: {
                            title: 'Create',
                            route: '/create',
                            controller: 'Isu.controller.CommunicationTasksEdit'
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}',
                            controller: 'Isu.controller.CommunicationTasksEdit'
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