Ext.define('Usr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation'
    ],

    config: {
        navigationController: null
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
        this.initNavigation();
        this.initDefaultHistoryToken();
        this.getApplication().on('changecontentevent', this.showContent, this);
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);

        //this.getViewport().setText(Uni.I18n.translate('user.application', 'USM', 'Jupiter system administration'));
    },

    initDefaultHistoryToken: function () {
        var setupController = this.getController('Usr.controller.history.Home'),
            eventBus = this.getController('Uni.controller.history.EventBus'),
            defaultToken = setupController.tokenizeShowOverview();

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