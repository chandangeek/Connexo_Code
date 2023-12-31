/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.controller.Task', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.privileges.BpmManagement',
        'Bpm.controller.FilterSortTasks',
        'Bpm.controller.OpenTask',
        'Uni.component.sort.model.Sort'
    ],
    views: [
        'Bpm.view.task.Tasks',
        'Bpm.view.task.ViewTask'
    ],
    stores: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksFilterDueDates',
        'Bpm.store.task.TasksFilterProcesses',
        'Bpm.store.task.TasksFilterStatuses',
        'Bpm.store.task.TasksFilterUsers',
        'Bpm.store.task.TasksFilterWorkgroups',
        'Bpm.store.task.TasksUsers',
        'Bpm.store.task.TasksFilterAllUsers',
        'Bpm.store.task.TaskWorkgroupAssignees',
        'Bpm.store.Clipboard'
    ],
    models: [
        'Bpm.model.task.Task',
        'Bpm.model.task.OpenTask'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'bpm-tasks'
        },
        {
            ref: 'mainGrid',
            selector: 'bpm-tasks bpm-tasks-grid'
        },
        {
            ref: 'viewTask',
            selector: 'bpm-task-view-task'
        }
    ],

    init: function () {
        this.control({
            'bpm-tasks bpm-tasks-grid': {
                select: this.showPreview
            },
            'bpm-task-action-menu': {
                show: this.onMenuShow,
                click: this.chooseAction
            },
            'bpm-task-action-menu #menu-claim-task': {
                click: this.claimTaskAction
            },
            'bpm-task-action-menu #menu-unassigned-task': {
                click: this.unassignTaskAction
            },
            'bpm-tasks-grid #btn-tasks-bulk-action': {
                click: this.forwardToBulk
            }
        });
        this.application.getController('Bpm.controller.FilterSortTasks');
    },

    showTask: function (taskId) {
        var me = this,
            store = me.getStore('Bpm.store.task.Tasks');
        if(store.count() === 0) {
            store.load({
                callback: function ()  {
                    me.performLoadOfTask(taskId);
                }
            });
        } else {
            me.performLoadOfTask(taskId)
        }
    },

    createTaskView: function(taskId, taskRecord){
        var me = this;
         var router = me.getController('Uni.controller.history.Router');
         var listLink = me.makeLinkToList(router);
         var performTask = me.getModel('Bpm.model.task.OpenTask');
         view = Ext.widget('bpm-task-view-task', {
                taskRecord: taskRecord,
                router: router,
                listLink: listLink
         });
         me.getController('Bpm.controller.OpenTask').taskId = taskId;
         me.getApplication().fireEvent('changecontentevent', view);
         view.down('form').loadRecord(taskRecord);
         view.down('#task-title').setTitle(taskRecord.get('name'));
         if (view.down('bpm-task-action-menu')) {
              me.setupMenuItems(taskRecord);
         }
         me.getApplication().fireEvent('task', taskRecord);
         performTask.load(taskId, {
              success: function (performTaskRecord) {
                    if (performTaskRecord && performTaskRecord.properties() && performTaskRecord.properties().count()) {
                            view.down('property-form').loadRecord(performTaskRecord);
                    } else {
                        if (taskRecord.get('status') === 'Completed') {
                            view.down('property-form').down('uni-form-empty-message').setText(Uni.I18n.translate('bpm.task.taskExecutionAttributesNotAvailableBecauseTaskCompleted', 'BPM', "Task execution attributes aren't available because task is completed."))
                        }
                        view.down('property-form').down('uni-form-empty-message').show();
                    }
              }
         })
    },

    performLoadOfTask: function (taskId) {
        var me = this,
            task = me.getModel('Bpm.model.task.Task');
        task.load(taskId, {
            success: function (taskRecord) {
                Ext.Ajax.request({
                    url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/nodes', taskRecord.get('processInstancesId')),
                    method: 'GET',
                    success: function (option) {
                        var response = Ext.JSON.decode(option.responseText),
                            reader = Bpm.monitorprocesses.model.ProcessNodes.getProxy().getReader(),
                            resultSet = reader.readRecords(response),
                            nodeRecord = resultSet.records[0];
                        var variable = null,
                            urlString = null,
                            usagePointVariable = me.findProcessVariable(nodeRecord, "usagePointId"),
                            deviceVariable = me.findProcessVariable(nodeRecord, "deviceId"),
                            issueVariable = me.findProcessVariable(nodeRecord, "issueId"),
                            alarmVariable = me.findProcessVariable(nodeRecord, "alarmId")

                        if (usagePointVariable) {
                            variable = usagePointVariable;
                            urlString = '/api/jsr/search/com.elster.jupiter.metering.UsagePoint';
                        }
                        if (deviceVariable) {
                            variable = deviceVariable;
                            urlString = '/api/jsr/search/com.energyict.mdc.common.device.data.Device';
                        }
                        if (issueVariable ){
                             variable = issueVariable;
                        }
                        if (alarmVariable ){
                             variable = alarmVariable;
                        }
                        if (issueVariable || alarmVariable){
                             taskRecord.set(variable.variableName, variable.value);
                             me.createTaskView(taskId, taskRecord);
                        } else if (variable) {
                                Ext.Ajax.request({
                                    method: 'GET',
                                    url: urlString,
                                    params: {
                                        filter: Ext.JSON.encode([{"property": "mRID", "value": [{"operator": "==", "criteria": variable.value, "filter": ""}]}])
                                    },
                                    success: function (option) {
                                        var responseUP = Ext.JSON.decode(option.responseText),
                                            searchResults = responseUP.searchResults;
                                        //console.log(searchResults);
                                        if (!Ext.isEmpty(searchResults)) {
                                            taskRecord.set(variable.variableName, searchResults[0].name);
                                            me.createTaskView(taskId, taskRecord);
                                        }
                                    }
                                });
                        }
                    }
                });

            }//
        });
    },

    makeLinkToList: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.tasks.title', 'BPM', 'Tasks').toLowerCase() + '</a>',
            filter = this.getStore('Bpm.store.Clipboard').get('latest-tasks-filter'),
            queryParams = filter ? filter : null;

        return Ext.String.format(link, router.getRoute('workspace/tasks').buildUrl(null, queryParams));
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
        } else if (queryString.param === 'myworkgroups') {
            Ext.Ajax.request({
                url: '/api/bpm/workgroups?myworkgroups=true',
                method: 'GET',
                success: function (response) {
                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                    if (decoded && decoded.workgroups) {
                        queryString.param = undefined;
                        queryString.user = [Uni.I18n.translate('general.userUnassigned', 'BPM', 'Unassigned')];
                        queryString.workgroup = decoded.workgroups.length == 0 ? [Uni.I18n.translate('general.workgroupUnassigned', 'BPM', 'Unassigned')] : decoded.workgroups.map(function (wg) {
                            return wg.name;
                        });
                        queryString.status = ['ASSIGNED', 'CREATED', 'ONGOING'];
                        queryString.sort = Ext.JSON.encode(sort);
                        window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    }
                }
            });
        }
        else if (queryString.param === 'myopentasks') {
            Ext.Ajax.request({
                url: '/api/bpm/runtime/assignees?me=true',
                method: 'GET',
                success: function (operation) {
                    queryString.param = undefined;
                    queryString.user = Ext.JSON.decode(operation.responseText).data[0].name;
                    queryString.status = ['ASSIGNED', 'CREATED', 'ONGOING'];
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
                    queryString.workgroup = [Uni.I18n.translate('general.workgroupUnassigned', 'BPM', 'Unassigned')];
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
                argProcess = router.arguments.process,
                argWorkgroup = router.arguments.workgroup;

            router.getRoute('workspace/tasks').params.use = false;
            delete queryString.param;
            argSort && (argSort != '') && (queryString.sort = argSort);
            argUser && (argUser != '') && (queryString.user = argUser);
            argDueDate && (argDueDate != '') && (queryString.dueDate = argDueDate);
            argStatus && (argStatus != '') && (queryString.status = argStatus);
            argProcess && (argProcess != '') && (queryString.process = argProcess);
            argWorkgroup && (argWorkgroup != '') && (queryString.workgroup = argWorkgroup);

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
        me.getStore('Bpm.store.Clipboard').set('latest-tasks-filter', queryString);
        me.getController('Bpm.controller.OpenTask').taskId = null;
    },

    findProcessVariable: function (processNodeRecord, variableName) {
        var nodes = processNodeRecord.processInstanceNodes();
        var continueLoop = true;
        var returnVariable = undefined;
        if (!Ext.isEmpty(nodes)) {
            nodes.each(function (item) {
                var variables = item.get('processInstanceVariables');
                if (!Ext.isEmpty(variables)) {
                    Ext.Array.each(variables, function (variable) {
                        if (variable.variableName === variableName) {
                            continueLoop = false;
                            returnVariable = variable;
                        }
                        return continueLoop;
                    });
                }
                return continueLoop;
            });
        }
        return returnVariable;
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('bpm-task-preview'),
            previewForm = page.down('bpm-task-preview-form');

        Ext.getStore('Bpm.store.Clipboard').set('latest-tasks-filter', Uni.util.QueryString.getQueryStringValues(false));
        Ext.Ajax.request({
            url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/nodes', record.get('processInstancesId')),
            method: 'GET',
            success: function (option) {
                var response = Ext.JSON.decode(option.responseText),
                    reader = Bpm.monitorprocesses.model.ProcessNodes.getProxy().getReader(),
                    resultSet = reader.readRecords(response),
                    nodeRecord = resultSet.records[0];
                var variable = null,
                    urlString = null,
                    usagePointVariable = me.findProcessVariable(nodeRecord, "usagePointId"),
                    deviceVariable = me.findProcessVariable(nodeRecord, "deviceId");

                if (usagePointVariable) {
                    variable = usagePointVariable;
                    urlString = '/api/jsr/search/com.elster.jupiter.metering.UsagePoint';
                }
                if (deviceVariable) {
                    variable = deviceVariable;
                    urlString = '/api/jsr/search/com.energyict.mdc.common.device.data.Device';
                }
                if (variable) {
                    Ext.Ajax.request({
                        method: 'GET',
                        url: urlString,
                        params: {
                            filter: Ext.JSON.encode([{"property": "mRID", "value": [{"operator": "==", "criteria": variable.value, "filter": ""}]}])
                        },
                        success: function (option) {
                            var responseUP = Ext.JSON.decode(option.responseText),
                                searchResults = responseUP.searchResults;
                            //console.log(searchResults);
                            if (!Ext.isEmpty(searchResults)) {

                                record.set(variable.variableName, searchResults[0].name);
                                previewForm.loadRecord(record);
                            }
                        }
                    });
                    // Ext.suspendLayouts();
                    // preview.setTitle(record.get('name'));
                    // previewForm.loadRecord(record);
                    // if (preview.down('bpm-task-action-menu')) {
                    //     preview.down('bpm-task-action-menu').record = record;
                    //     me.setupMenuItems(record);
                    // }
                    // Ext.resumeLayouts();

                }
            }
        });
        //console.log("After get");
        //console.log(record);

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        if (preview.down('bpm-task-action-menu')) {
            preview.down('bpm-task-action-menu').record = record;
            me.setupMenuItems(record);
        }
        Ext.resumeLayouts();
    },

    setupMenuItems: function (record) {
        var ongoing = record.get('status') === 'InProgress',
            completeItems = Ext.ComponentQuery.query('menu menuitem[action=completeTask]'),
            performItems = Ext.ComponentQuery.query('menu menuitem[action=performTask]');

        if (!Ext.isEmpty(completeItems)) {
            Ext.Array.each(completeItems, function (item) {
                item.setVisible(ongoing);
            });
        }

        if (!Ext.isEmpty(performItems)) {
            Ext.Array.each(performItems, function (item) {
                item.setVisible(!ongoing);
            });
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            route,
            record;

        if ((item.action == 'assignToMeTask') || (item.action == 'unassignedTask')) {
            return;
        }

        record = menu.record || me.getMainGrid().getSelectionModel().getLastSelected();
        router.arguments.taskId = record.get('id');

        switch (item.action) {
            case 'editTask':
                route = 'workspace/tasks/task/editTask';
                break;
            case 'performTask':
                route = 'workspace/tasks/task/performTask';
                break;
            case 'completeTask':
                route = 'workspace/tasks/task/completeTask';
                break;
        }

        //
        route && (route = router.getRoute(route));
        route.params.sort = queryString.sort;
        route.params.user = queryString.user;
        route.params.dueDate = queryString.dueDate;
        route.params.status = queryString.status;
        route.params.process = queryString.process;
        route.params.workgroup = queryString.workgroup;

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
        tasksRoute.params.workgroup = undefined;

        queryString.sort && (queryString.sort != '') && (tasksRoute.params.sort = queryString.sort);
        queryString.user && (queryString.user != '') && (tasksRoute.params.user = queryString.user);
        queryString.dueDate && (queryString.dueDate != '') && (tasksRoute.params.dueDate = queryString.dueDate);
        queryString.status && (queryString.status != '') && (tasksRoute.params.status = queryString.status);
        queryString.process && (queryString.process != '') && (tasksRoute.params.process = queryString.process);
        queryString.workgroup && (queryString.workgroup != '') && (tasksRoute.params.workgroup = queryString.workgroup);

        route ='workspace/tasks/bulkaction';

        route && (route = router.getRoute(route));
        route.params.sort = queryString.sort;
        route.params.user = queryString.user;
        route.params.dueDate = queryString.dueDate;
        route.params.status = queryString.status;
        route.params.process = queryString.process;
        route.params.workgroup = queryString.workgroup;

        route && route.forward(router.arguments);

    },

    updateApplyButtonState: function (view, queryString) {
        view.down('button[action=clearAll]').setDisabled(!((queryString.hasOwnProperty('sort') && Object.keys(queryString).length > 1) || (!queryString.hasOwnProperty('sort') && Object.keys(queryString).length > 0)));
    },

    onMenuShow: function (menu) {
        var me = this,
            userTask, loggedUser,
            record = menu.record || me.getMainGrid().getSelectionModel().getLastSelected();
        menu.setLoading();
        menu.down('#menu-claim-task').hide();
        menu.down('#menu-unassigned-task').hide();
        Ext.Ajax.request({
            url: '/api/bpm/runtime/assignees?me=true',
            method: 'GET',
            success: function (response) {
                var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                loggedUser = decoded && decoded.data && decoded.data.length > 0 ? decoded.data[0].name : '';
                Ext.Ajax.request({
                    url: '/api/bpm/runtime/tasks/' + record.get('id'),
                    method: 'GET',
                    success: function (response) {
                        var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                        userTask = decoded ? decoded.actualOwner : null;

                        if (userTask === loggedUser) {
                            menu.down('#menu-claim-task').hide();
                            menu.down('#menu-unassigned-task').show();
                            menu.down('#menu-unassigned-task').record = record;
                        }
                        else {
                            menu.down('#menu-unassigned-task').hide();
                            menu.down('#menu-claim-task').show();
                            menu.down('#menu-claim-task').record = record;
                        }
                        menu.setLoading(false);
                    }
                });
            }
        });
    },

    claimTaskAction: function (menuItem) {
        var me = this,
            record = menuItem.record;

        Ext.Ajax.request({
            url: '/api/bpm/runtime/assigntome/' + record.get('id'),
            method: 'POST',
            success: function (response) {
                var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;

                Ext.Ajax.request({
                    url: '/api/bpm/runtime/assignees?me=true',
                    method: 'GET',
                    success: function (response) {
                        var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                        loggedUser = decoded && decoded.data && decoded.data.length > 0 ? decoded.data[0].name : '';

                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editProcess.successMsg.assigned', 'BPM', 'Task assigned'));
                        if (me.getMainGrid()) {
                            me.getMainGrid().getStore().load();
                        }
                        else if (me.getViewTask()) {
                            var task = me.getModel('Bpm.model.task.Task'),
                                form = me.getViewTask().down('form');

                            form.setLoading();
                            task.load(record.get('id'), {
                                success: function (taskRecord) {
                                    form.loadRecord(taskRecord);
                                    form.setLoading(false);
                                },
                                failure: function () {
                                    form.setLoading(false);
                                }
                            })
                        }
                    }
                });

            }
        });
    },

    unassignTaskAction: function (menuItem) {
        var me = this,
            record = menuItem.record;

        Ext.Ajax.request({
            url: '/api/bpm/runtime/release/' + record.get('id'),
            method: 'POST',
            success: function (response) {
                var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;

                Ext.Ajax.request({
                    url: '/api/bpm/runtime/assignees?me=true',
                    method: 'GET',
                    success: function (response) {
                        var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                        loggedUser = decoded && decoded.data && decoded.data.length > 0 ? decoded.data[0].name : '';

                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editProcess.successMsg.unassigned', 'BPM', 'Task was unassigned'));
                        if (me.getMainGrid()) {
                            me.getMainGrid().getStore().load();
                        }
                        else if (me.getViewTask()) {
                            var task = me.getModel('Bpm.model.task.Task'),
                                form = me.getViewTask().down('form');

                            form.setLoading();
                            task.load(record.get('id'), {
                                success: function (taskRecord) {
                                    form.loadRecord(taskRecord);
                                    form.setLoading(false);
                                },
                                failure: function () {
                                    form.setLoading(false);
                                }
                            })
                        }
                    }
                });

            }
        });
    }
});