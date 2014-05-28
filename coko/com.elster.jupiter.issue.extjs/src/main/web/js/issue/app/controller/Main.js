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
        'Isu.controller.Issues',
        'Isu.controller.AssignIssues',
        'Isu.controller.CloseIssues',
        'Isu.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow',
        'Isu.controller.IssueAssignmentRules',
        'Isu.controller.IssueCreationRules',
        'Isu.controller.history.Workspace',
        'Isu.controller.IssueDetail',
        'Isu.controller.history.Administration',
        'Isu.controller.AdministrationDataCollection',
        'Isu.controller.Notify'
    ],

    stores: [
        'Isu.store.Issues'
    ],

    config: {
        navigationController: null,
        configurationController: null
    },

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
        this.getApplication().on('changecontentevent', this.showContent, this);
        this.initNavigation();
        this.initMenu();
    },

    initMenu: function () {
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

        var datacollection = Ext.create('Uni.model.PortalItem', {
            title: 'Data collection',
            portal: 'workspace',
            route: 'datacollection',
            items: [
                {
                    text: 'Overview',
                    href: '#/workspace/datacollection'
                },
                {
                    text: 'Issues',
                    href: '#/workspace/datacollection/issues'
                }
            ]
        });

//        var dataexchange = Ext.create('Uni.model.PortalItem', {
//            title: 'Data exchange',
//            portal: 'workspace',
//            route: 'dataexchange',
//            items: [
//                {
//                    text: 'Overview',
//                    href: '#/workspace/dataexchange'
//                },
//                {
//                    text: 'Issues',
//                    href: '#/workspace/dataexchange/issues'
//                }
//            ]
//        });
//        var dataoperation = Ext.create('Uni.model.PortalItem', {
//            title: 'Data operation',
//            portal: 'workspace',
//            route: 'dataoperation',
//            items: [
//                {
//                    text: 'Overview',
//                    href: '#/workspace/dataoperation'
//                },
//                {
//                    text: 'Issues',
//                    href: '#/workspace/dataoperation/issues'
//                }
//            ]
//        });
//
//        var datavalidation = Ext.create('Uni.model.PortalItem', {
//            title: 'Data validation',
//            portal: 'workspace',
//            route: 'datavalidation',
//            items: [
//                {
//                    text: 'Overview',
//                    href: '#/workspace/datavalidation'
//                },
//                {
//                    text: 'Issues',
//                    href: '#/workspace/datavalidation/issues'
//                }
//            ]
//        });

        var issuemanagement = Ext.create('Uni.model.PortalItem', {
            title: 'Issue management',
            portal: 'administration',
            route: 'issuemanagement',
            items: [
                {
                    text: 'Issue assignment rules',
                    href: '#/administration/issueassignmentrules'
                },
                {
                    text: 'Issue creation rules',
                    href: '#/administration/issuecreationrules'
                }
            ]
        });

        Uni.store.PortalItems.add(
            datacollection,
//            dataexchange,
//            dataoperation,
//            datavalidation,
            issuemanagement
        );
    },

    initNavigation: function () {
        var navigationController = this.getController('Uni.controller.Navigation'),
            configurationController = this.getController('Uni.controller.Configuration');

        this.setNavigationController(navigationController);
        this.setConfigurationController(configurationController);
    },

    showContent: function (widget) {
        this.clearContentPanel();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    clearContentPanel: function () {
        var widget;
        while (widget = this.getContentPanel().items.first()) {
            this.getContentPanel().remove(widget, false);
        }
    }
});