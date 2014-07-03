Ext.define('Bpm.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Bpm.controller.ProcessInstances',
        'Bpm.controller.history.BpmManagement'
    ],

    controllers: [
        'Bpm.controller.ProcessInstances',
        'Bpm.controller.history.BpmManagement'
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
        var me = this;
        this.initNavigation();

        var response = Ext.Ajax.request({
            async: false,
            url: '../../api/bpm/runtime/startup'
        });
        var items = Ext.decode(response.responseText);

        var bpm = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('bpm.instance.title', 'BPM', 'Processes'),
            portal: 'workspace',
            route: 'workspace',
            items: [
                {
                    text: Uni.I18n.translate('bpm.console', 'BPM', 'Console'),
                    href: items.url,
                    hrefTarget: '_blank'
                },
                {
                    text: Uni.I18n.translate('bpm.instance.title', 'BPM', 'Processes'),
                    href: '#workspace/processes'
                }
            ]
        });

        Uni.store.PortalItems.add(
            bpm
        );
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    showContent: function (widget) {
        this.clearContentPanel();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    clearContentPanel: function () {
        this.getContentPanel().removeAll(false);
    }
});