Ext.define('Bpm.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Bpm.controller.Task',
        'Bpm.controller.TaskBulk',
        'Bpm.controller.history.BpmManagement',
        'Bpm.privileges.BpmManagement'
    ],

    controllers: [
        'Bpm.controller.history.BpmManagement',
        'Bpm.controller.Task'
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
                route: 'taskmanagement',
                items: [
                    {
                        text: Uni.I18n.translate('general.taskmanagement.tasks', 'BPM', 'Tasks'),
                        itemId: 'tasks',
                        href: router.getRoute('workspace/tasks').buildUrl({}, {param: 'noFilter'})
                    },
                    {
                        text: Uni.I18n.translate('general.taskmanagement.myopentasks', 'BPM', 'My open tasks'),
                        itemId: 'my-open-tasks',
                        href: router.getRoute('workspace/tasks').buildUrl({}, {param: 'myopentasks'})
                    },
                    {
                        text: Uni.I18n.translate('general.taskmanagement.unassignedtask', 'BPM', 'Unassigned tasks'),
                        itemId: 'unassigned-tasks',
                        href: router.getRoute('workspace/tasks').buildUrl({}, {param: 'unassign'})
                    },
                    {
                        text: Uni.I18n.translate('general.taskmanagement.overduetasks', 'BPM', 'Overdue tasks'),
                        itemId: 'overdue-tasks',
                        href: router.getRoute('workspace/tasks').buildUrl({}, {param: 'dueDate'})
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