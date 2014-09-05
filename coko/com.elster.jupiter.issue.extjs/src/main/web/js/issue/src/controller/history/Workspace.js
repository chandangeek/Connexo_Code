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
                    items: {
                        issues: {
                            title : 'Issues',
                            route: 'issues',
                            controller: 'Isu.controller.Issues',
                            items: {
                                view: {
                                    title: 'issue details',
                                    route: '{id}',
                                    controller: 'Isu.controller.IssueDetail'
                                },
                                edit: {
                                    title: 'Issue Edit',
                                    route: '{id}/addcomment',
                                    controller: 'Isu.controller.IssueDetail'
                                },
                                assign: {
                                    title: 'Issue Assign',
                                    route: '{id}/assign',
                                    controller: 'Isu.controller.AssignIssues'
                                },
                                close: {
                                    title: 'Issue Close',
                                    route: '{id}/close',
                                    controller: 'Isu.controller.CloseIssues'
                                },
                                notify: {
                                    title: 'Notify user',
                                    route: '{id}/notify',
                                    controller: 'Isu.controller.NotifySend',
                                    action: 'showNotifySend'
                                },
                                send: {
                                    title: 'Send to inspect',
                                    route: '{id}/send',
                                    controller: 'Isu.controller.NotifySend',
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
                }
            }
        }
    },

    init :function() {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
