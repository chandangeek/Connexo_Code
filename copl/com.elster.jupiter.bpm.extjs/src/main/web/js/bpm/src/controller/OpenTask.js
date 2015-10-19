Ext.define('Bpm.controller.OpenTask', {
    extend: 'Ext.app.Controller',
    stores: [
        'Bpm.store.task.Priorities'
    ],

    models: [
        'Bpm.model.task.TaskEdit',
        'Bpm.model.task.Assign'
    ],
    views: [
        'Bpm.view.task.OpenTask'
    ],
    refs: [
        {
            ref: 'openTaskPage',
            selector: 'bpm-task-open-task'
        },
        {
            ref: 'formContent',
            selector: 'bpm-task-open-task #formContent'
        },
        {
            ref: 'assigneeUserForm',
            selector: 'bpm-task-open-task #frm-assignee-user'
        },
        {
            ref: 'editTaskForm',
            selector: 'bpm-task-open-task #frm-edit-task'
        },
        {
            ref: 'formContainer',
            selector: 'bpm-task-open-task #frm-form-container'
        },
        {
            ref: 'btnClaim',
            selector: 'bpm-task-open-task #btn-claim'
        },
        {
            ref: 'btnRelease',
            selector: 'bpm-task-open-task #btn-release'
        },
        {
            ref: 'btnStart',
            selector: 'bpm-task-open-task #btn-start'
        },
        {
            ref: 'btnSave',
            selector: 'bpm-task-open-task #btn-save'
        },
        {
            ref: 'btnComplete',
            selector: 'bpm-task-open-task #btn-complete'
        },
        {
            ref: 'btnTaskActions',
            selector: 'bpm-task-open-task #btn-taskactions'
        }


    ],
    listener: null,
    loadingObject: null,

    init: function () {
        this.control({
            'bpm-task-open-task #btn-assignee-user-save': {
                click: this.saveAssigneeUser
            },
            'bpm-task-open-task #btn-task-save': {
                click: this.saveTask
            },
            'bpm-task-open-task #btn-claim': {
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-release': {
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-start': {
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-save': {
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-complete': {
                click: this.chooseAction
            }

        });
        var me = this;
        listener = function dolisten(event) {

            me.getFormContainer().setLoading(false);
            var task = me.getModel('Bpm.model.task.Task');
            task.load(me.getOpenTaskPage().taskRecord.get('id'), {
                success: function (taskRecord) {
                    me.refreshButtons(taskRecord);
                }
            });
        }
        if (window.addEventListener) {
            addEventListener("message", listener, false)
        } else {
            attachEvent("onmessage", listener)
        }
    },

    showOpenTask: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/taksmanagementtasks'),
            openTaskView, openTaskForm, taskRecord, queryParams = {};

        sort = router.arguments.sort;
        user = router.arguments.user;
        dueDate = router.arguments.dueDate;
        taskStatus = router.arguments.status;
        process = router.arguments.process;

        var tasksRoute = router.getRoute('workspace/taksmanagementtasks');
        tasksRoute.params.sort = tasksRoute.params.user = tasksRoute.params.dueDate = tasksRoute.params.status =
            tasksRoute.params.process = undefined;
        tasksRoute.params.use = true;

        sort && (sort != '') && (queryParams.sort = tasksRoute.params.sort = sort);
        user && (user != '') && (queryParams.user = tasksRoute.params.user = user);
        dueDate && (dueDate != '') && (queryParams.dueDate = tasksRoute.params.dueDate = dueDate);
        taskStatus && (taskStatus != '') && (queryParams.status = tasksRoute.params.status = taskStatus);
        process && (process != '') && (queryParams.process = tasksRoute.params.process = process);

        var task = me.getModel('Bpm.model.task.Task');
        task.load(taskId, {
            success: function (taskRecord) {

                openTaskView = Ext.create('Bpm.view.task.OpenTask', {
                    //    returnLink: router.getRoute('workspace/taksmanagementtasks').buildUrl({}, queryParams),
                    taskRecord: taskRecord
                });

                openTaskView.taskRecord = taskRecord;
                me.getApplication().fireEvent('openTask', taskRecord);

                openTaskForm = openTaskView.down('#frm-open-task');
                openTaskForm.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.openTaskTitle', 'BPM', "'{0}' task"), taskRecord.get('name')));

                //openTaskForm.loadRecord(taskRecord);
                me.getApplication().fireEvent('changecontentevent', openTaskView);


                me.loadAssigneeForm(taskRecord);
                me.loadEditTaskForm(taskRecord);
                me.loadBpmForm(taskRecord);



            },
            failure: function (record, operation) {
            }
        });
    },

    loadAssigneeForm: function (taskRecord) {
        var me = this,
            assigneeForm = me.getAssigneeUserForm();

        if (!assigneeForm){
            return;
        }
        var assigneeCombo = assigneeForm.down('#cbo-assignee-user');
        assigneeCombo.store.load({
            callback: function (records, operation, success) {
                assigneeForm.loadRecord(taskRecord);
            }
        });
    },

    saveAssigneeUser: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            assignUser = Ext.create('Bpm.model.task.Assign'),
            assigneeForm = me.getAssigneeUserForm();

        assignUser.getProxy().extraParams = { username: assigneeForm.down('#cbo-assignee-user').getValue()};
        assignUser.getProxy().setUrl(taskRecord.get('id'));
        assigneeForm.setLoading();
        assignUser.save({
            success: function () {
                assigneeForm.setLoading(false);
            },
            failure: function (record, operation) {
                assigneeForm.setLoading(false);
            }
        })
    },

    loadEditTaskForm: function (taskRecord) {
        var me = this,
            editTaskForm = me.getEditTaskForm();

        editTaskForm && editTaskForm.loadRecord(taskRecord);
    },

    saveTask: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            taskEdit = Ext.create('Bpm.model.task.TaskEdit'),
            editTaskForm = me.getEditTaskForm();

        editTaskForm.setLoading();
        editTaskForm.updateRecord(taskRecord);
        taskEdit.getProxy().extraParams = { priority: editTaskForm.down('#cbo-priority').getValue(),
            duedate: editTaskForm.down('#due-date').getValue() ? moment(editTaskForm.down('#due-date').getValue()).valueOf() : ''};

        taskEdit.getProxy().setUrl(taskRecord.get('id'));
        taskEdit.save({
            success: function () {
                editTaskForm.setLoading(false);
            },
            failure: function (record, operation) {
                editTaskForm.setLoading(false);
            }
        })
    },

    loadBpmForm: function(taskRecord){
        var me = this,
            flowUrl;

        if (!me.getFormContainer()){
            return;
        }

        me.getFormContainer().setLoading();
        var rowIndex = Uni.store.Apps.findExact('name', 'Flow');
        if (rowIndex != -1) {
            flowUrl = Uni.store.Apps.getAt(rowIndex).get('url');
            Ext.Ajax.request({
                url: flowUrl + '/rest/task/' + taskRecord.get('id') + '/showTaskForm',
                method: 'GET',
                success: function (operation) {
                    try {
                        var xmlDoc = me.getXMLDoc(operation.responseText);

                        if (!xmlDoc) {
                            return;
                        }
                        var status = xmlDoc.getElementsByTagName("status");

                        if (status && status.length > 0 && status[0].childNodes.length > 0) {
                            status = status[0].childNodes[0].nodeValue;

                            var openTaskPage = me.getOpenTaskPage(),
                                formContent = me.getFormContent();

                            if (status == 'SUCCESS') {
                                var formURL = xmlDoc.getElementsByTagName("formUrl");
                                if (formURL && formURL.length > 0 && formURL[0].childNodes.length > 0) {
                                    openTaskPage.formURL = formURL[0].childNodes[0].nodeValue;
                                    var html = "<iframe id='iframeId' src='" + openTaskPage.formURL + "' frameborder='0' style='width:100%; height:100%'></iframe>";

                                    formContent.getEl().dom.innerHTML = html;

                                    document.getElementById("iframeId").addEventListener('load', function () {
                                        me.getFormContainer().setLoading(false);
                                    });
                                    //me.resizeReportPanel(formContent);
                                    me.refreshButtons(taskRecord);

                                }
                            }

                        }

                    } catch (err) {
                        me.getFormContainer().setLoading(false);
                    }
                }
            })

        }
    },

    getXMLDoc: function (xml) {
        if (!xml) return;

        var xmlDoc;
        if (window.DOMParser) {
            var parser = new DOMParser();
            xmlDoc = parser.parseFromString(xml, "text/xml");
        } else { // Internet Explorer
            xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
            xmlDoc.async = false;
            xmlDoc.loadXML(xml);
        }
        return xmlDoc;
    },

    resizeReportPanel: function (component) {

        if (!component)
            return;

        var options = {};
        options.element = component.getEl().dom;
        options.height = component.getHeight() - 38;
        options.width = component.getWidth() - 3;
        options.showTitle = false;
        if (options.showTitle)
            options.height -= 30;
    },

    refreshButtons: function (taskRecord) {
        var me = this,
            status = taskRecord.get('status');

        me.getBtnClaim().setVisible(status == "Ready");
        me.getBtnRelease().setVisible((status == "Reserved") || (status == "InProgress"));
        me.getBtnStart().setVisible((status == "Reserved"));
        me.getBtnSave().setVisible(status == "InProgress");
        me.getBtnComplete().setVisible(status == "InProgress");
        me.getBtnTaskActions().setVisible(false);
    },

    chooseAction: function (button, item) {
        var me = this,
            action = button.action,
            taskRecord = button.taskRecord;

        var rowIndex = Uni.store.Apps.findExact('name', 'Flow');
        if (rowIndex != -1) {
            flowUrl = Uni.store.Apps.getAt(rowIndex).get('url');
            var frame = document.getElementById('iframeId').contentWindow;

            me.getFormContainer().setLoading();
            var request = '{"action":"' + action + '","taskId":"' + taskRecord.get('id') + '"}';
            frame.postMessage(request, me.getOpenTaskPage().formURL);
        }
    }

});