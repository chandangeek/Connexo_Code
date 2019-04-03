/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Itk.privileges.Issue',
        'Itk.controller.Overview',
        'Itk.controller.CreationRules',
        'Itk.controller.CreationRuleEdit',
        'Itk.controller.CreationRuleActionEdit'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        "workspace/issues": {
            title: Uni.I18n.translate('device.issues', 'ITK', 'Issues'),
            route: 'workspace/issues',
            controller: 'Itk.controller.Issues',
            action: 'showOverview',
            privileges: Itk.privileges.Issue.viewAdminIssue,
            items: {
                bulkaction: {
                    title: Uni.I18n.translate('general.bulkAction','ITK','Bulk action'),
                    route: 'bulkaction',
                    privileges: Itk.privileges.Issue.viewAdminIssue,
                    controller: 'Itk.controller.BulkChangeIssues'
                },
                view: {
                    title: Uni.I18n.translate('general.issueDetails', 'ITK', 'Issue details'),
                    route: '{issueId}',
                    controller: 'Itk.controller.Detail',
                    action: 'showOverview',
                    privileges: Itk.privileges.Issue.viewAdminIssue,
                    callback: function (route) {
                        this.getApplication().on('issueLoad', function (record) {
                            route.setTitle(record.get('title'));
                            return true;
                        }, {single: true});
                        return this;
                    },

                    items: {
                        startProcess: {
                            title: Uni.I18n.translate('general.startProcess', 'ITK', 'Start process'),
                            route: 'startProcess',
                            controller: 'Itk.controller.StartProcess',
                            action: 'showStartProcess',
                            privileges: Itk.privileges.Issue.viewAdminProcesses,
                        },
                        viewProcesses: {
                            title: Uni.I18n.translate('general.processes', 'ITK', 'Processes'),
                            route: 'processes',
                            controller: 'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses',
                            action: 'showIssueProcesses',
                            privileges: Itk.privileges.Issue.viewAdminProcesses,
                            params: {
                                process: '',

                            },
                        },
                        setpriority:{
                            title: Uni.I18n.translate('general.setpriority','ITK','Set priority'),
                            route: 'setpriority',
                            controller: 'Itk.controller.SetPriority',
                            action: 'setPriority',
                            privileges: Itk.privileges.Issue.viewAdminIssue
                        },
                        action: {
                            title: Uni.I18n.translate('general.action', 'ITK', 'Action'),
                            route: 'action/{actionId}',
                            controller: 'Itk.controller.ApplyAction',
                            privileges: Itk.privileges.Issue.viewAdminIssue,
                            callback: function (route) {
                                this.getApplication().on('issueActionLoad', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }

                    }
                }
            }

        },
        "workspace/issuesoverview":{
            title: Uni.I18n.translate('device.issues.overview', 'ITK', 'Issues overview'),
            route: 'workspace/issuesoverview',
            controller: 'Itk.controller.Overview',
            action: 'showIssueOverview',
            privileges: Itk.privileges.Issue.viewAdminIssue
        },
        administration : {
            title: Uni.I18n.translate('route.administration', 'ITK', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                issuecreationrules: {
                    title: Uni.I18n.translate('general.issueCreationRules', 'ITK', 'Issue creation rules'),
                    route: 'issuecreationrules',
                    controller: 'Itk.controller.CreationRules',
                    privileges: Itk.privileges.Issue.viewAdminIssueCreationRule,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.issueCreationRules.add', 'ITK', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Itk.controller.CreationRuleEdit',
                            privileges: Itk.privileges.Issue.createIssueRule,
                            action: 'showEdit',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'ITK', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Itk.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                }
                            }
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'ITK', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Itk.controller.CreationRuleEdit',
                            action: 'showEdit',
                            privileges: Itk.privileges.Issue.createIssueRule,
                            callback: function (route) {
                                this.getApplication().on('issueCreationRuleEdit', function (record) {
                                    route.setTitle(Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'ITK', "Edit '{0}'", record.get('name'), false));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'ITK', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Itk.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
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
