Ext.define('Idc.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Isu.privileges.Issue',
        'Isu.controller.IssuesOverview'
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
                issues: {
                    title: Uni.I18n.translate('general.issues', 'IDC', 'Issues'),
                    route: 'issues',
                    controller: 'Isu.controller.IssuesOverview',
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
                            title: Uni.I18n.translate('general.issueDetails','IDC','Issue details'),
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
                                },
                                startProcess: {
                                    title: Uni.I18n.translate('general.startProcess','IDC','Start process'),
                                    route: 'startProcess',
                                    controller: 'Isu.controller.StartProcess',
                                    action: 'showStartProcess',
                                    privileges: Isu.privileges.Issue.viewAdminProcesses
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
