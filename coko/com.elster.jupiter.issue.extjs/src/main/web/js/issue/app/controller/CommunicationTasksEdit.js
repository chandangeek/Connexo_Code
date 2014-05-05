Ext.define('Isu.controller.CommunicationTasksEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CommunicationTasks'
    ],

    views: [
        'Isu.view.administration.communicationtasks.Edit'
    ],

    refs: [
        {
            ref: 'taskEdit',
            selector: 'communication-tasks-edit'
        }
    ],

    init: function () {
        this.control({
            'communication-tasks-edit breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            }
        });
    },

    showOverview: function (id) {
        var widget = Ext.widget('communication-tasks-edit');

        if (id) {
            this.operationType = 'Edit';
        } else {
            this.operationType = 'Create';
        }

        this.getApplication().fireEvent('changecontentevent', widget);
        this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this,
            breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Communication tasks',
                href: 'communicationtasks'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: me.operationType + ' communication task'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});