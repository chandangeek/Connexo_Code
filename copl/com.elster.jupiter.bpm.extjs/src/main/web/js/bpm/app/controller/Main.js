Ext.define('Bpm.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Bpm.controller.ProcessInstances',
        'Bpm.controller.history.BpmManagement',
        'Bpm.model.Startup'
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
        Bpm.model.Startup.load(null, {
            success: function(record, operation) {
                var bpmUrl = record.get("url");
                var menuItem = Ext.create('Uni.model.MenuItem', {
                    text: 'Bpm management',
                    glyph: 'settings',
                    portal: 'bpmmanagement',
                    index: -10
                });

                Uni.store.MenuItems.add(menuItem);

                var bpm = Ext.create('Uni.model.PortalItem', {
                    title: 'Bpm management',
                    portal: 'bpmmanagement',
                    route: 'bpmmanagement',
                    items: [
                        {
                            text: 'Bpm console',
                            href: bpmUrl,
                            hrefTarget: '_blank'
                        },
                        {
                            text: 'Process instances',
                            href: '#bpmmanagement/instances'
                        }
                    ]
                });

                Uni.store.PortalItems.add(
                    bpm
                );
            }
        });
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