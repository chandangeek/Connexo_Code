Ext.define('Bpm.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Bpm.controller.Task',
        'Bpm.controller.TaskBulk',
        'Bpm.processes.controller.Processes',
        'Bpm.controller.history.BpmManagement',
        'Bpm.privileges.BpmManagement'
    ],

    controllers: [
        'Bpm.controller.history.BpmManagement',
        'Bpm.controller.Task',
        'Bpm.controller.TaskBulk'
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
        var me = this,
            historian = me.getController('Bpm.controller.history.BpmManagement'); // Forces route registration.
    },


    addProcessManagement: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'BPM', 'Administration'),
            portal: 'administration',
            glyph: 'settings',
            index: 10
        });

        Uni.store.MenuItems.add(menuItem);

        if (Bpm.privileges.BpmManagement.canViewProcesses()) {
            var processManagement = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.processManagement', 'BPM', 'Process management'),
                portal: 'administration',

                items: [
                    {
                        text: Uni.I18n.translate('general.managementprocesses.processes', 'BPM', 'Processes'),
                        itemId: 'processes',
                        href: '#/administration/managementprocesses'
                    }
                ]
            });
            Uni.store.PortalItems.add(processManagement);
        }
    },

    initNavigtaion: function () {
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