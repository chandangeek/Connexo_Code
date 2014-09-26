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
        'Isu.controller.history.Workspace',
        'Isu.controller.history.Administration',
        'Isu.controller.Issues',
        'Isu.controller.AssignIssues',
        'Isu.controller.CloseIssues',
        'Isu.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow',
        'Isu.controller.IssueAssignmentRules',
        'Isu.controller.IssueCreationRules',
        'Isu.controller.IssueDetail',
        'Isu.controller.AdministrationDataCollection',
        'Isu.controller.NotifySend',
        'Isu.controller.DataValidation',
        'Isu.controller.IssueCreationRulesEdit',
        'Isu.controller.IssueCreationRulesActionsEdit',
        'Isu.controller.DataCollectionOverview'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.Users'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this;

        var workspaceItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            glyph: 'workspace',
            portal: 'workspace',
            index: 30
        });

        Uni.store.MenuItems.add(workspaceItem);

        var administrationItem = Ext.create('Uni.model.MenuItem', {
            text: 'Administration',
            glyph: 'settings',
            portal: 'administration',
            index: 10
        });

        Uni.store.MenuItems.add(administrationItem);

        var router = me.getController('Uni.controller.history.Router'),
            historian0 = me.getController('Isu.controller.history.Workspace'), // Forces route registration.
            historian1 = me.getController('Isu.controller.history.Administration'); // Forces route registration.

        var datacollection = Ext.create('Uni.model.PortalItem', {
            title: 'Data collection',
            portal: 'workspace',
            route: 'datacollection',
            items: [
                {
                    text: 'Overview',
                    href: router.getRoute('workspace/datacollection').buildUrl()
                },
                {
                    text: 'Issues',
                    href: router.getRoute('workspace/datacollection/issues').buildUrl()
                },
                {
                    text: 'My open issues',
                    handler: function () {
                        router.getRoute('workspace/datacollection/issues').forward();
                        me.getController('Isu.controller.Issues').fireEvent('showIssuesAssignedOnMe');
                    }
                }
            ]
        });

        var datavalidation = Ext.create('Uni.model.PortalItem', {
            title: 'Data validation',
            portal: 'workspace',
            route: 'datavalidation',
            items: [
                {
                    text: 'Overview',
                    href: '#/workspace/datavalidation'
                },
                {
                    text: 'Issues',
                    href: '#/workspace/datavalidation/issues'
                }
            ]
        });

        var issuemanagement = Ext.create('Uni.model.PortalItem', {
            title: 'Issue management',
            portal: 'administration',
            route: 'issuemanagement',
            items: [
                {
                    text: 'Issue assignment rules',
                    href: router.getRoute('administration/assignmentrules').buildUrl()
                },
                {
                    text: 'Issue creation rules',
                    href: router.getRoute('administration/creationrules').buildUrl()
                }
            ]
        });

        Uni.store.PortalItems.add(
            datacollection,
            datavalidation,
            issuemanagement
        );
    }
});