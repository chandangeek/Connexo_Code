Ext.define('Bpm.controller.Task', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.privileges.BpmManagement',
        'Bpm.controller.FilterSortTasks',
        'Bpm.controller.OpenTask',
        'Uni.component.sort.model.Sort'
    ],
    views: [
        'Bpm.view.task.Tasks'
    ],
    stores: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksFilterDueDates',
        'Bpm.store.task.TasksFilterProcesses',
        'Bpm.store.task.TasksFilterStatuses',
        'Bpm.store.task.TasksFilterUsers',
        'Bpm.store.task.TasksUsers'
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
            },
            'bpm-task-action-menu': {
                click: this.chooseAction
            },
            'bpm-tasks-grid #btn-tasks-bulk-action': {
                click: this.forwardToBulk
            }
        });
        this.application.getController('Bpm.controller.FilterSortTasks');
    },

    showTasks: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            filterSortController = this.application.getController('Bpm.controller.FilterSortTasks'),
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
                    queryString.status = ['ASSIGNED', 'CREATED'];
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
        else if (router.getRoute('workspace/tasks').params.use === true){

            var argSort = router.arguments.sort, argUser = router.arguments.user,
                argDueDate = router.arguments.dueDate, argStatus = router.arguments.status,
                argProcess = router.arguments.process;

            router.getRoute('workspace/tasks').params.use = false;
            delete queryString.param;
            argSort && (argSort != '') && (queryString.sort = argSort);
            argUser && (argUser != '') && (queryString.user = argUser);
            argDueDate && (argDueDate != '') && (queryString.dueDate = argDueDate);
            argStatus && (argStatus != '') && (queryString.status = argStatus);
            argProcess && (argProcess != '') && (queryString.process = argProcess);

            if (Uni.util.QueryString.buildHrefWithQueryString(queryString, false) != location.href) {
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
            }
            else {
               var store = Ext.getStore('Bpm.store.task.Tasks');
               store.loadData([], false);
                view = Ext.widget('bpm-tasks', {
                    router: router
                });
                me.getApplication().fireEvent('onBeforeLoad', view);
                me.getApplication().fireEvent('changecontentevent', view);
                filterSortController.updateSortingToolbar();
                me.updateApplyButtonState(view, queryString);
            }
        }
        else {
            var store = Ext.getStore('Bpm.store.task.Tasks');
            store.loadData([], false);
            view = Ext.widget('bpm-tasks', {
                router: router
            });
            me.getApplication().fireEvent('onBeforeLoad', view);
            me.getApplication().fireEvent('changecontentevent', view);
            filterSortController.updateSortingToolbar();
            me.updateApplyButtonState(view, queryString);
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
        preview.down('bpm-task-action-menu') && (preview.down('bpm-task-action-menu').record = record);
        Ext.resumeLayouts();
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            route,
            record;

        record = menu.record || me.getMainGrid().getSelectionModel().getLastSelected();
        router.arguments.taskId = record.get('id');

        switch (item.action) {
            case 'editTask':
                route = 'workspace/tasks/editTask';
                break;
            case 'performTask':
                route = 'workspace/tasks/performTask';
                break;
        }

        //
        route && (route = router.getRoute(route));
        route.params.sort = queryString.sort;
        route.params.user = queryString.user;
        route.params.dueDate = queryString.dueDate;
        route.params.status = queryString.status;
        route.params.process = queryString.process;

        route && route.forward(router.arguments);
    },

    forwardToBulk: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            route;

        var tasksRoute = router.getRoute('workspace/tasks');
        tasksRoute.params.sort = undefined;
        tasksRoute.params.user = undefined;
        tasksRoute.params.dueDate = undefined;
        tasksRoute.params.status = undefined;
        tasksRoute.params.process = undefined;

        queryString.sort && (queryString.sort != '') && (tasksRoute.params.sort = queryString.sort);
        queryString.user && (queryString.user != '') && (tasksRoute.params.user = queryString.user);
        queryString.dueDate && (queryString.dueDate != '') && (tasksRoute.params.dueDate = queryString.dueDate);
        queryString.status && (queryString.status != '') && (tasksRoute.params.status = queryString.status);
        queryString.process && (queryString.process != '') && (tasksRoute.params.process = queryString.process);

        route ='workspace/tasks/bulkaction';

        route && (route = router.getRoute(route));
        route.params.sort = queryString.sort;
        route.params.user = queryString.user;
        route.params.dueDate = queryString.dueDate;
        route.params.status = queryString.status;
        route.params.process = queryString.process;

        route && route.forward(router.arguments);

    },

    updateApplyButtonState: function (view, queryString) {
        view.down('button[action=clearAll]').setDisabled(!((queryString.hasOwnProperty('sort') && Object.keys(queryString).length > 1) || (!queryString.hasOwnProperty('sort') && Object.keys(queryString).length > 0)));
    }
});