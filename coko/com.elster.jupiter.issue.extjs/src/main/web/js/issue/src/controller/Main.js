Ext.define('Isu.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Isu.privileges.Issue'
    ],

    controllers: [
        'Isu.controller.history.Administration',
        'Isu.controller.AssignmentRules',
        'Isu.controller.CreationRules',
        'Isu.controller.CreationRuleEdit',
        'Isu.controller.CreationRuleActionEdit',
        'Isu.controller.IssuesOverview',
        'Isu.controller.IssueDetail',
        'Isu.controller.ApplyIssueAction',
        'Isu.controller.Overview'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            issuemanagement = null,
            issuemanagementItems = [],
            issuesPortalItem,
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Isu.controller.history.Administration'); // Forces route registration.

        if (Isu.privileges.Issue.canAdminRule()){
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration','ISU','Administration'),
                glyph: 'settings',
                portal: 'administration',
                index: 10
            }));
        }

        if (Isu.privileges.Issue.canAdminRule()){
            if(Isu.privileges.Issue.canViewRule()) {
                issuemanagementItems.push(
                    {
                        text: Uni.I18n.translate('issue.administration.assignment','ISU','Issue assignment rules'),
                        href: router.getRoute('administration/assignmentrules').buildUrl()
                    }
                );
            }
            if(Isu.privileges.Issue.canAdminCreateRule()) {
                issuemanagementItems.push(
                    {
                        text: Uni.I18n.translate('general.issueCreationRules','ISU','Issue creation rules'),
                        href: router.getRoute('administration/creationrules').buildUrl()
                    }
                );
            }
            issuemanagement = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.issueManagement','ISU','Issue management'),
                portal: 'administration',
                route: 'issuemanagement',
                items: issuemanagementItems
            });
        }

        if (Isu.privileges.Issue.canViewAdminDevice()) {
            issuesPortalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
                portal: 'workspace',
                route: 'issues',
                items: [
                    {
                        text: Uni.I18n.translate('workspace.issues.title','ISU','Issues'),
                        itemId: 'issues-item',
                        href: router.getRoute('workspace/issues').buildUrl()
                    },
                    {
                        text: Uni.I18n.translate('workspace.issues.issuesOverview', 'ISU', 'Issues overview'),
                        itemId: 'issues-overview-item',
                        href: router.getRoute('workspace/issuesoverview').buildUrl()
                    }
                ]
            });
        }

        if (issuesPortalItem !== null) {
            Uni.store.PortalItems.add(issuesPortalItem);
        }

        if (issuemanagement !== null) {
            Uni.store.PortalItems.add(issuemanagement);
        }

        me.getApplication().on('initIssueType', function (type) {
            if (type == 'datacollection') {
                me.getController('Isu.controller.IssuesOverview').dataCollectionActivated = true;
                me.getController('Isu.controller.ApplyIssueAction').dataCollectionActivated = true;
            } else if (type == 'datavalidation') {
                me.getController('Isu.controller.IssuesOverview').dataValidationActivated = true;
                me.getController('Isu.controller.ApplyIssueAction').dataValidationActivated = true;
            }
        });
    }
});