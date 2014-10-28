Ext.define('Idc.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                datacollection: {
                    title: 'Data collection',
                    route: 'datacollection',
                    controller: 'Idc.controller.MainOverview',
                    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                    items: {
                        issues: {
                            title: 'Issues',
                            route: 'issues',
                            controller: 'Idc.controller.Overview',
                            action: 'showOverview',
                            filter: 'Isu.model.IssuesFilter',
                            privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                            items: {
                                bulkaction: {
                                    title: 'Bulk action',
                                    route: 'bulkaction',
                                    controller: 'Idc.controller.BulkChangeIssues'
                                },
                                view: {
                                    title: 'issue details',
                                    route: '{issueId}',
                                    controller: 'Idc.controller.Detail',
                                    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
                                    callback: function (route) {
                                        this.getApplication().on('issueLoad', function (record) {
                                            route.setTitle(record.get('title'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        action: {
                                            title: 'Action',
                                            route: 'action/{actionId}',
                                            controller: 'Idc.controller.ApplyAction',
                                            privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
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
