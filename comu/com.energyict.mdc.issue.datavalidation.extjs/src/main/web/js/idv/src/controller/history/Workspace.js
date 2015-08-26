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
            title: Uni.I18n.translate('general.workspace','IDV','Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                datavalidationissues: {
                    title: Uni.I18n.translate('breadcrumb.datavalidationissues', 'IDV', 'Data validation issues'),
                    route: 'datavalidationissues',
                    controller: 'Idv.controller.Overview',
                    action: 'showOverview',
                    privileges: Isu.privileges.Issue.viewAdminDevice,
                    items: {
                        bulkaction: {
                            title: Uni.I18n.translate('general.bulkAction','IDV','Bulk action'),
                            route: 'bulkaction',
                            privileges: Isu.privileges.Issue.closeOrAssing,
                            controller: 'Idv.controller.BulkChangeIssues'
                        },
                        view: {
                            title: Uni.I18n.translate('general.issueDetails','IDV','Issue details'),
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
                                    title: Uni.I18n.translate('general.action','IDV','Action'),
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
                },
                datavalidationoverview: {
                    title: Uni.I18n.translate('validation.validationOverview.title', 'IDV', 'Validation overview'),
                    route: 'datavalidationoverview',
                    controller: 'Ddv.controller.ValidationOverview',
                    action: 'showOverview',
                    filter: 'Ddv.model.ValidationOverviewFilter'
                }
            }

        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
