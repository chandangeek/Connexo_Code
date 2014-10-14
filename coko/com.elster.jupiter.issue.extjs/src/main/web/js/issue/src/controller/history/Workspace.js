Ext.define('Isu.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace : {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                datacollection: {
                    title: 'Data collection',
                    route: 'datacollection',
                    controller: 'Isu.controller.DataCollectionOverview',
                    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                    items: {
                        issues: {
                            title : 'Issues',
                            route: 'issues',
                            controller: 'Isu.controller.Issues',
                            action: 'showDataCollection',
                            privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                            items: {
                                view: {
                                    title: 'issue details',
                                    route: '{id}',
                                    controller: 'Isu.controller.IssueDetail',
                                    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue']
                                },
                                edit: {
                                    title: 'Issue Edit',
                                    route: '{id}/addcomment',
                                    controller: 'Isu.controller.IssueDetail',
                                    privileges: ['privilege.comment.issue']
                                },
                                assign: {
                                    title: 'Issue Assign',
                                    route: '{id}/assign',
                                    controller: 'Isu.controller.AssignIssues',
                                    privileges: ['privilege.assign.issue']
                                },
                                close: {
                                    title: 'Issue Close',
                                    route: '{id}/close',
                                    controller: 'Isu.controller.CloseIssues',
                                    privileges: ['privilege.close.issue']
                                },
                                notify: {
                                    title: 'Notify user',
                                    route: '{id}/notify',
                                    controller: 'Isu.controller.NotifySend',
                                    privileges: ['privilege.action.issue'],
                                    action: 'showNotifySend'
                                },
                                send: {
                                    title: 'Send to inspect',
                                    route: '{id}/send',
                                    controller: 'Isu.controller.NotifySend',
                                    privileges: ['privilege.action.issue'],
                                    action: 'showNotifySend'
                                }
                            }
                        },
                        bulk: {
                            title : 'Bulk Changes',
                            route: 'bulkaction',
                            controller: 'Isu.controller.BulkChangeIssues'
                        }
                    }
                },
                datavalidation: {
                    title: Uni.I18n.translate('router.datavalidation', 'ISU', 'Data validation'),
                    route: 'datavalidation',
                    controller: 'Isu.controller.DataValidation',
                    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                    action: 'showOverview',
                    items: {
                        issues: {
                            title : 'Issues',
                            route: 'issues',
                            controller: 'Isu.controller.Issues',
                            privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                            action: 'showDataValidation',
                            items: {
                                view: {
                                    title: 'issue details',
                                    route: '{id}',
                                    controller: 'Isu.controller.IssueDetail',
                                    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue']
                                }
                            }
                        },
                        bulk: {
                            title : 'Bulk Changes',
                            route: 'bulkaction',
                            controller: 'Isu.controller.BulkChangeIssues'
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
