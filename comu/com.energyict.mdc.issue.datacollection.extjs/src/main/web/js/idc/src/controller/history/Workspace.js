Ext.define('Idc.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Isu.privileges.Issue'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace','IDC','Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                datacollectionissues: {
                    title: Uni.I18n.translate('breadcrumb.datacollectionissues', 'IDC', 'Data collection issues'),
                    route: 'datacollectionissues',
                    controller: 'Idc.controller.Overview',
                    action: 'showOverview',
                    privileges: Isu.privileges.Issue.viewAdminDevice,
                    items: {
                        bulkaction: {
                            title: Uni.I18n.translate('general.bulkAction','IDC','Bulk action'),
                            route: 'bulkaction',
                            privileges: Isu.privileges.Issue.closeOrAssing,
                            controller: 'Idc.controller.BulkChangeIssues'
                        },
                        view: {
                            title: Uni.I18n.translate('general.issueDetails','IDC','issue details'),
                            route: '{issueId}',
                            controller: 'Idc.controller.Detail',
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
                                    title: Uni.I18n.translate('general.action','IDC','Action'),
                                    route: 'action/{actionId}',
                                    controller: 'Idc.controller.ApplyAction',
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
