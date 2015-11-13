Ext.define('Bpm.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Bpm.controller.Task',
        'Bpm.controller.TaskBulk',
        'Bpm.controller.Process',
        'Bpm.controller.history.BpmManagement',
        'Bpm.privileges.BpmManagement'
    ],

    controllers: [
        'Bpm.controller.history.BpmManagement',
        'Bpm.controller.Task',
        'Bpm.controller.Process'
    ],

    stores: [
        'Bpm.store.process.Processes'
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

        me.addTaskManagement();
        me.addProcessManagement();
    },

    addTaskManagement: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null;

        if (Bpm.privileges.BpmManagement.canView()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'BPM', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Bpm.privileges.BpmManagement.canView()) {
            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.taskManagement', 'BPM', 'Task management'),
                portal: 'workspace',
                route: 'taksmanagement',
                items: [
                    {
                        text: Uni.I18n.translate('general.taksmanagement.tasks', 'BPM', 'Tasks'),
                        itemId: 'tasks',
                        href: router.getRoute('workspace/taksmanagementtasks').buildUrl({}, {param: 'noFilter'})
                    },
                    {
                        text: Uni.I18n.translate('general.taksmanagement.myopentasks', 'BPM', 'My open tasks'),
                        itemId: 'my-open-tasks',
                        href: router.getRoute('workspace/taksmanagementtasks').buildUrl({}, {param: 'myopentasks'})
                    },
                    {
                        text: Uni.I18n.translate('general.taksmanagement.unassignedtask', 'BPM', 'Unassigned tasks'),
                        itemId: 'unassigned-tasks',
                        href: router.getRoute('workspace/taksmanagementtasks').buildUrl({}, {param: 'unassign'})
                    },
                    {
                        text: Uni.I18n.translate('general.taksmanagement.overduetasks', 'BPM', 'Overdue tasks'),
                        itemId: 'overdue-tasks',
                        href: router.getRoute('workspace/taksmanagementtasks').buildUrl({}, {param: 'dueDate'})
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
        }
    },
    addProcessManagement: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null;

        if (Bpm.privileges.BpmManagement.canViewProcesses()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'BPM', 'Administration'),
                glyph: 'settings',
                portal: 'administration',
                index: 10
            }));
        }

        if (Bpm.privileges.BpmManagement.canViewProcesses()) {
            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.processManagement', 'BPM', 'Process management'),
                portal: 'administration',
                route: 'managementprocesses',
                items: [
                    {
                        text: Uni.I18n.translate('general.managementprocesses.processes', 'BPM', 'Processes'),
                        itemId: 'processes',
                        href: router.getRoute('administration/managementprocesses').buildUrl()
                    }
                ]
            });
        }

        if (dataCollection !== null) {
            Uni.store.PortalItems.add(dataCollection);
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