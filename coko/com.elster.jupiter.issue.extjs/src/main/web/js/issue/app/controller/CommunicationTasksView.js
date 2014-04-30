Ext.define('Isu.controller.CommunicationTasksView', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Isu.model.CommunicationTasksSort'
    ],

    stores: [
        'Isu.store.CommunicationTasks'
    ],

    views: [
        'Isu.view.administration.communicationtasks.View'
    ],

    mixins: [
        'Isu.util.IsuGrid'
    ],

    refs: [
        {
            ref: 'tasksView',
            selector: 'communication-tasks-view'
        },
        {
            ref: 'itemPanel',
            selector: 'issues-item'
        }
    ],

    init: function () {
        this.control({
            'communication-tasks-view breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'communication-tasks-view communication-tasks-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.onCommunicationTasksGridRefresh
            },
            'communication-tasks-view communication-tasks-list actioncolumn': {
                click: this.showItemAction
            },
            'communication-tasks-action-menu': {
                beforehide: this.hideItemAction,
                click: this.chooseCommunicationTasksAction
            },
            'communication-tasks-view communication-tasks-sort-toolbar communication-tasks-sort-menu': {
                click: this.addSortParam
            }
        });

        this.actionMenuXtype = 'communication-tasks-action-menu';
        this.gridItemModel = this.getModel('Isu.model.CommunicationTasks');
    },

    showOverview: function () {
        var widget = Ext.widget('communication-tasks-view');

        this.getApplication().fireEvent('changecontentevent', widget);
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
            });
        breadcrumbParent.setChild(breadcrumbChild1);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    addSortParam: function (menu, item) {
        var sorting = new Isu.model.CommunicationTasksSort();

        sorting.addSortParam(item.action);
        this.getTasksView().down('communication-tasks-sort-toolbar').addSortButtons(sorting);
    },

    chooseCommunicationTasksAction: function (menu, item) {
        var action = item.action;

        switch (action) {
            case 'edit':

                break;
            case 'delete':

                break;
        }
    },

    onCommunicationTasksGridRefresh: function (grid) {
        var store = grid.getStore();

        if (!store.getCount()) {
            this.selectFirstGridRow(grid);
        }
    }
});