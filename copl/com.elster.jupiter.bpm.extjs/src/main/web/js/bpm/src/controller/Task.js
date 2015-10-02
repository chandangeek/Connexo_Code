Ext.define('Bpm.controller.Task', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.privileges.BpmManagement',
        'Bpm.controller.FilterSortTasks'
    ],
    views: [
        'Bpm.view.task.Tasks'
    ],
    stores: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksFilterDueDates',
        'Bpm.store.task.TasksFilterProcesses',
        'Bpm.store.task.TasksFilterStatuses',
        'Bpm.store.task.TasksFilterUsers'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'bpm-tasks'
        },
        {
            ref: 'mainGrid',
            selector: 'bpm-tasks bpm-tasks-grid'
        }
    ],

    init: function () {
        this.control({
            'bpm-tasks bpm-tasks-grid': {
                select: this.showPreview
            }/*,
             'bpm-task-action-menu': {
             click: this.chooseAction
             }*/
        });
        this.application.getController('Bpm.controller.FilterSortTasks');
    },

    showTasks: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            filterSortContoller = this.application.getController('Bpm.controller.FilterSortTasks'),
            view, sort = [];

        sort = [{property: 'dueDate', direction: Uni.component.sort.model.Sort.DESC},
            {property: 'creationDate', direction: Uni.component.sort.model.Sort.DESC}];

        if (queryString.param === 'noFilter') {
            queryString.param = undefined;
            queryString.sort = Ext.JSON.encode(sort);
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        }
        else if (queryString.param === 'myopentasks') {
            Ext.Ajax.request({
                url: '/api/bpm/runtime/assignees?me=true',
                method: 'GET',
                success: function (operation) {
                    queryString.param = undefined;
                    queryString.user = Ext.JSON.decode(operation.responseText).data[0].name;
                    queryString.sort = Ext.JSON.encode(sort);
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                }
            })
        }
        else if (queryString.param === 'unassign') {
            var usersStore = me.getStore('Bpm.store.task.TasksFilterUsers');
            usersStore.load({
                callback: function (records, operation, success) {
                    var rowIndex = usersStore.findExact('id', -1);
                    queryString.user = usersStore.getAt(rowIndex).get('name');

                    queryString.param = undefined;
                    queryString.sort = Ext.JSON.encode(sort);
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                }
            });
        }
        else if (queryString.param === 'dueDate') {
            queryString.param = undefined;
            queryString.dueDate = 'OVERDUE';
            queryString.sort = Ext.JSON.encode(sort);
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        }
        else {
            view = Ext.widget('bpm-tasks', {
                router: router
            });

            me.getApplication().fireEvent('changecontentevent', view);
            filterSortContoller.updateSortingToolbar();
            //filterSortContoller.setDefaultSort();
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('bpm-task-preview'),
            previewForm = page.down('bpm-task-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        //   preview.down('bpm-task-action-menu').record = record;
        Ext.resumeLayouts();
    }
    /*,

     chooseAction: function (menu, item) {
     var me = this,
     router = me.getController('Uni.controller.history.Router'),
     route;

     router.arguments.importServiceId = menu.record.get('importServiceId');
     router.arguments.occurrenceId = menu.record.get('occurrenceId');
     if (item.action === 'viewLog') {
     route = 'administration/importservices/importservice/history/occurrence';
     }

     route && (route = router.getRoute(route));
     route && route.forward(router.arguments);
     }*/
});