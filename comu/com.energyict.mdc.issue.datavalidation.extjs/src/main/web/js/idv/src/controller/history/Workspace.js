Ext.define('Idv.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Isu.privileges.Issue'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                datavalidationissues: {
                    title: Uni.I18n.translate('breadcrumb.datavalidationissues', 'IDC', 'Data validation issues'),
                    route: 'datavalidationissues',
                    controller: 'Idv.controller.Overview',
                    action: 'showOverview',
                    filter: 'Isu.model.IssuesFilter',
                    privileges: Isu.privileges.Issue.viewAdminDevice,
                    items: {
                        bulkaction: {
                            title: 'Bulk action',
                            route: 'bulkaction',
                            privileges: Isu.privileges.Issue.closeOrAssing,
                            controller: 'Idv.controller.BulkChangeIssues'
                        },
                        view: {
                            title: 'issue details',
                            route: '{issueId}',
                            controller: 'Idv.controller.Detail',
                            privileges: Isu.privileges.Issue.viewAdminDevice,
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
                                    controller: 'Idv.controller.ApplyAction',
                                    privileges: Isu.privileges.Issue.viewAdminDevice,
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
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
