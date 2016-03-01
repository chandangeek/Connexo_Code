Ext.define('Dbp.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems'
    ],

    controllers: [
        'Dbp.controller.History',
        'Dbp.startprocess.controller.StartProcess',
        'Dbp.monitorprocesses.controller.MonitorProcesses'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Dbp.controller.History'); // Forces route registration.

        me.getApplication().fireEvent('cfginitialized');
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
    }
});