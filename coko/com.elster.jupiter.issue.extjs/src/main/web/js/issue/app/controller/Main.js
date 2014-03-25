Ext.define('Isu.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Isu.controller.history.Workspace'
    ],

    controllers: [
        'Isu.controller.Main',
        'Isu.controller.Workspace',
        'Isu.controller.DataCollection',
        'Isu.controller.Issues',
        'Isu.controller.AssignIssues',
        'Isu.controller.CloseIssues',
        'Isu.controller.BulkChangeIssues',
        'Isu.controller.MessageWindow',
        'Isu.controller.IssueAssignmentRules',
        'Isu.controller.IssueCreationRules',
        'Isu.controller.Licensing',
        'Isu.controller.AddLicense',
        'Isu.controller.history.Workspace',
        'Isu.controller.IssueFilter',
        'Isu.controller.IssueDetail',
        'Isu.controller.history.Administration',
        'Isu.controller.Administration',
        'Isu.controller.AdministrationDataCollection'
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
        this.initMenu();
        this.initNavigation();
        this.initDefaultHistoryToken();
        this.getApplication().on('changecontentevent', this.showContent, this);
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Workspace',
            href: '#/workspace',
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Administration',
            href: '#/administration',
            glyph: 'xe011@icomoon'
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
            this.getContentPanel().remove(widget, true);
        }
    }
});