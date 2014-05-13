Ext.define('Isu.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus'
    ],

    controllers: [
        'Isu.controller.history.Workspace',
        'Isu.controller.Workspace',
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
        'Isu.controller.Administration',
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
        this.initDefaultHistoryToken();
        this.initNavigation();
        this.initMenu();
    },

    initMenu: function () {
        var me = this;

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            href: me.getController('Isu.controller.history.Workspace').tokenizeShowOverview(),
            glyph: 'workspace'
        });

        Uni.store.MenuItems.add(menuItem);

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Administration',
            href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview(),
            glyph: 'settings'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    initNavigation: function () {
        var navigationController = this.getController('Uni.controller.Navigation'),
            configurationController = this.getController('Uni.controller.Configuration');

        this.setNavigationController(navigationController);
        this.setConfigurationController(configurationController);
    },

    initDefaultHistoryToken: function () {
        var workspaceController = this.getController('Isu.controller.history.Workspace'),
            eventBus = this.getController('Uni.controller.history.EventBus'),
            defaultToken = workspaceController.tokenizeShowOverview();

        eventBus.setDefaultToken(defaultToken);
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