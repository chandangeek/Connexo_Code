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

        crossroads.addRoute('workspace/datacollection', function () {
            me.getController('Isu.controller.DataCollectionOverview').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues', function () {
            me.getController('Isu.controller.Issues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issuesbulkaction', function () {
            me.getController('Isu.controller.BulkChangeIssues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}', function (id) {
            me.getController('Isu.controller.IssueDetail').showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/addcomment', function (id) {
            me.getController('Isu.controller.IssueDetail').showOverview(id, true);
        });
        crossroads.addRoute('workspace/datacollection/bulkaction', function () {
            me.getController('Isu.controller.BulkChangeIssues').showOverview();
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/assign', function (id) {
            me.getController('Isu.controller.AssignIssues').showOverview(id);
        });
        crossroads.addRoute('workspace/datacollection/issues/{id}/close', function (id) {
            me.getController('Isu.controller.CloseIssues').showOverview(id);
        });

        this.callParent(arguments);
    }
});
