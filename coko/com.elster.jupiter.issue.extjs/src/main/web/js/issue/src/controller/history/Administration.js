Ext.define('Isu.controller.history.Administration', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Isu.privileges.Issue'
    ],
    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'ISU', 'Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                issues: {
                    title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
                    route: 'issues',
                    controller: 'Isu.controller.IssuesOverview',
                    action: 'showOverview',
                    privileges: Isu.privileges.Issue.viewAdminDevice
                }
            }
        },
        administration : {
            title: Uni.I18n.translate('route.administration', 'ISU', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                assignmentrules: {
                    title: Uni.I18n.translate('issue.administration.assignment', 'ISU', 'Issue assignment rules'),
                    route: 'assignmentrules',
                    controller: 'Isu.controller.AssignmentRules',
                    privileges: Isu.privileges.Issue.viewRule
                },
                creationrules: {
                    title: Uni.I18n.translate('general.issueCreationRules', 'ISU', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.CreationRules',
                    privileges: Isu.privileges.Issue.adminCreateRule,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.issueCreationRules.add', 'ISU', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.CreationRuleEdit',
                            privileges: Isu.privileges.Issue.createRule,
                            action: 'showEdit',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'ISU', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                }
                            }
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit','ISU','Edit'),
                            route: '{id}/edit',
                            controller: 'Isu.controller.CreationRuleEdit',
                            action: 'showEdit',
                            privileges: Isu.privileges.Issue.createRule,
                            callback: function (route) {
                                this.getApplication().on('issueCreationRuleEdit', function (record) {
                                    route.setTitle(Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'ISU', "Edit '{0}'", record.get('name'), false));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'ISU', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                }
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