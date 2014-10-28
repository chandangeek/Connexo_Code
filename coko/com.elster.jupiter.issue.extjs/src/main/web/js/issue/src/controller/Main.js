Ext.define('Isu.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Isu.controller.history.Administration',
        'Isu.controller.AssignmentRules',
        'Isu.controller.CreationRules',
        'Isu.controller.CreationRuleEdit',
        'Isu.controller.CreationRuleActionEdit',
        'Isu.controller.IssuesOverview',
        'Isu.controller.IssueDetail',
        'Isu.controller.ApplyIssueAction'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            issuemanagement = null,
            issuemanagementItems = [],
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Isu.controller.history.Administration'); // Forces route registration.

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.assignmentRule','privilege.administrate.creationRule','privilege.view.creationRule'])){
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: 'Administration',
                glyph: 'settings',
                portal: 'administration',
                index: 10
            }));
        }

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.assignmentRule','privilege.administrate.creationRule','privilege.view.creationRule'])){
            if(Uni.Auth.hasAnyPrivilege(['privilege.view.assignmentRule'])) {
                issuemanagementItems.push(
                    {
                        text: 'Issue assignment rules',
                        href: router.getRoute('administration/assignmentrules').buildUrl()
                    }
                );
            }
            if(Uni.Auth.hasAnyPrivilege(['privilege.administrate.creationRule','privilege.view.creationRule'])) {
                issuemanagementItems.push(
                    {
                        text: 'Issue creation rules',
                        href: router.getRoute('administration/creationrules').buildUrl()
                    }
                );
            }
            issuemanagement = Ext.create('Uni.model.PortalItem', {
                title: 'Issue management',
                portal: 'administration',
                route: 'issuemanagement',
                items: issuemanagementItems
            });
        }

        if (issuemanagement !== null) {
            Uni.store.PortalItems.add(issuemanagement);
        }
    }
});