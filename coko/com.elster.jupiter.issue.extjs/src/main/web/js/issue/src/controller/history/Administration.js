Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Isu.privileges.Issue'
    ],
    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        administration : {
            title: Uni.I18n.translate('route.administration', 'ISU', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                assignmentrules: {
                    title: Uni.I18n.translate('route.assignmentRules', 'ISU', 'Assignment Rules'),
                    route: 'assignmentrules',
                    controller: 'Isu.controller.AssignmentRules',
                    privileges: Isu.privileges.Issue.viewRule
                },
                creationrules: {
                    title: Uni.I18n.translate('route.issueCreationRules', 'ISU', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.CreationRules',
                    privileges: Isu.privileges.Issue.adminCreateRule,
                    items: {
                        add: {
                            title: Uni.I18n.translate('route.addIssueCreationRule', 'ISU', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.CreationRuleEdit',
                            privileges: Isu.privileges.Issue.createRule,
                            action: 'showCreate',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('route.addAction', 'ISU', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.CreationRuleActionEdit',
                                    action: 'showCreate'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Isu.controller.CreationRuleEdit',
                            action: 'showEdit',
                            privileges: Isu.privileges.Issue.createRule,
                            callback: function (route) {
                                this.getApplication().on('issueCreationRuleEdit', function (record) {
                                    route.setTitle('Edit' + " '" + record.data.name + "'");
                                    return true;
                                }, {single: true});

                                return this;
                            }
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