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
        'Isu.controller.DataCollection',
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
        'Isu.controller.AdministrationDataCollection'
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
        var me = this;

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            glyph: 'workspace',
            portal: 'workspace'
        });

        Uni.store.MenuItems.add(menuItem);

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: 'Workspace',
            component: 'Isu.view.workspace.Menu',
            portal: 'workspace'
        });

        var portalItem2 = Ext.create('Uni.model.PortalItem', {
            title: 'Administration',
            component: 'Isu.view.administration.Menu',
            portal: 'workspace'
        });

        Uni.store.PortalItems.add(
            portalItem1,
            portalItem2
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