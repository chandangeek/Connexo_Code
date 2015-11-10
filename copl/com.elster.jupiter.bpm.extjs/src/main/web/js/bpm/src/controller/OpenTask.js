Ext.define('Bpm.controller.OpenTask', {
    extend: 'Ext.app.Controller',
    stores: [],

    models: [
        'Bpm.model.task.TaskEdit',
        'Bpm.model.task.Assign',
        'Bpm.model.task.OpenTask'
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
            ref: 'numPriority',
            selector: 'bpm-task-open-task #num-priority'
        },
        {
            ref: 'priorityDisplay',
            selector: 'bpm-task-open-task #priority-display'
        },

        {
            ref: 'aboutTaskForm',
            selector: 'bpm-task-open-task #frm-about-task'
        },
        {
            ref: 'formContainer',
            selector: 'bpm-task-open-task #frm-form-container'
        },
        {
            ref: 'taskExecutionContent',
            selector: 'bpm-task-open-task #task-execution-content'
        },
        {
            ref: 'taskExecutionForm',
            selector: 'bpm-task-open-task #task-execution-form'
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
            },
            'bpm-task-open-task #num-priority': {
                change: this.updatePriority
            }
        });
        /*var me = this;
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
        }*/
    },

    showOpenTask: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/taksmanagementtasks'),
            openTaskView, topTitle, taskRecord, queryParams = {};

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
                    itemNameLink: '<a href="' + tasksRoute.buildUrl({}, queryParams) + '">' + Uni.I18n.translate('bpm.task.tasksName', 'BPM', 'tasks') + '</a>',
                    router: me.getController('Uni.controller.history.Router'),
                    taskRecord: taskRecord
                });

                openTaskView.taskRecord = taskRecord;
                me.getApplication().fireEvent('openTask', taskRecord);

                topTitle = openTaskView.down('#detail-top-title');
                topTitle.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.openTaskTitle', 'BPM', "'{0}' task"), taskRecord.get('name')));

                //openTaskForm.loadRecord(taskRecord);
                me.getApplication().fireEvent('changecontentevent', openTaskView);


                me.loadAssigneeForm(taskRecord);
                me.loadEditTaskForm(taskRecord);
                me.loadAboutTaskForm(taskRecord);
                me.loadJbpmForm(taskRecord);
                //    me.loadBpmForm(taskRecord);


            },
            failure: function (record, operation) {
            }
        });
    },

    loadAssigneeForm: function (taskRecord) {
        var me = this,
            assigneeForm = me.getAssigneeUserForm();

        if (!assigneeForm) {
            return;
        }
        var assigneeCombo = assigneeForm.down('#cbo-assignee-user');
        assigneeCombo.store.load({
            callback: function (records, operation, success) {
                assigneeForm.loadRecord(taskRecord);
            }
        });
    },

    loadEditTaskForm: function (taskRecord) {
        var me = this,
            editTaskForm = me.getEditTaskForm(),
            numPriority = me.getNumPriority();

        editTaskForm && editTaskForm.loadRecord(taskRecord);
    },

    saveTask: function (button) {
        var me = this;

        me.saveAssigneeUser(button);
        me.saveEditTask(button);
    },

    saveAssigneeUser: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            assignUser = Ext.create('Bpm.model.task.Assign'),
            assigneeForm = me.getAssigneeUserForm();

        assignUser.getProxy().extraParams = {username: assigneeForm.down('#cbo-assignee-user').getValue()};
        assignUser.getProxy().setUrl(taskRecord.get('id'));
        assigneeForm.setLoading();
        assignUser.save({
            success: function () {
                assigneeForm.setLoading(false);
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    assigneeForm.getForm().markInvalid(json.errors);
                }

                assigneeForm.setLoading(false);
            }
        })
    },

    saveEditTask: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            taskEdit = Ext.create('Bpm.model.task.TaskEdit'),
            editTaskForm = me.getEditTaskForm();

        editTaskForm.setLoading();
        editTaskForm.updateRecord(taskRecord);
        taskEdit.getProxy().extraParams = {
            priority: editTaskForm.down('#num-priority').getValue(),
            duedate: editTaskForm.down('#due-date').getValue() ? moment(editTaskForm.down('#due-date').getValue()).valueOf() : ''
        };

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

    loadAboutTaskForm: function (taskRecord) {
        var me = this,
            aboutTaskForm = me.getAboutTaskForm();

        aboutTaskForm && aboutTaskForm.loadRecord(taskRecord);
    },

    updatePriority: function (control, newValue, oldValue) {
        var me = this,
            label = '';

        if (newValue <= 3) {
            label = Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High');
        }
        else if (newValue <= 7) {
            label = Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium');
        }
        else {
            label = Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low');
        }

        me.getPriorityDisplay().setText(label);
    },

    loadJbpmForm: function (taskRecord) {
        var me = this,
            taskExecutionContent = me.getTaskExecutionContent(),
            openTask = me.getModel('Bpm.model.task.OpenTask'),
            propertyForm = taskExecutionContent.down('grouped-property-form');


        taskExecutionContent.setLoading();

        openTask.load(taskRecord.get('id'), {
            success: function (openTaskRecord) {

                taskExecutionContent.openTaskRecord = openTaskRecord;
                if (openTaskRecord && openTaskRecord.properties() && openTaskRecord.properties().count()) {
                    propertyForm.loadRecord(openTaskRecord);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }
                propertyForm.up('#frm-open-task').doLayout();
                me.refreshButtons(openTaskRecord);
                taskExecutionContent.setLoading(false);
            },
            failure: function (record, operation) {
            }
        });

    },

    refreshButtons: function (taskRecord) {
        var me = this,
            status = taskRecord.get('status');

        //me.getBtnClaim().setVisible(status == "Ready");
        me.getBtnRelease().setVisible((status == "Reserved") || (status == "InProgress"));
        me.getBtnStart().setVisible((status == "Reserved"));
        me.getBtnSave().setVisible(status == "InProgress");
        me.getBtnComplete().setVisible(status == "InProgress");
    },

    chooseAction: function (button, item) {
        var me = this,
            action = button.action,
            taskExecutionForm = me.getTaskExecutionForm(),
            taskExecutionContent = me.getTaskExecutionContent(),
            openTaskRecord = taskExecutionContent.openTaskRecord,
            propertyForm = taskExecutionContent.down('grouped-property-form');

        propertyForm.updateRecord();

        openTaskRecord.beginEdit();
        if (propertyForm.getRecord()) {
            openTaskRecord.propertiesStore = propertyForm.getRecord().properties();
        }
        openTaskRecord.set('action', action);
        openTaskRecord.endEdit();

        // remove abocve code
        var task = me.getModel('Bpm.model.task.Task');
        task.load(openTaskRecord.get('id'), {
            success: function (taskRecord) {
                me.loadAssigneeForm(taskRecord);
                me.loadEditTaskForm(taskRecord);
                me.loadAboutTaskForm(taskRecord);
                me.loadJbpmForm(taskRecord);
            }
        });
/*
        openTaskRecord.save({
            success: function () {

                //if (button.action === 'claimTask') {
                //    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.claimed', 'BPM', 'Task claimed.'));
                //} else
                if (button.action === 'saveTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.saved', 'BPM', 'Task saved.'));
                } else if (button.action === 'releaseTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.released', 'BPM', 'Task released.'));
                } else if (button.action === 'startTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.started', 'BPM', 'Task started.'));
                } else if (button.action === 'completeTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.completed', 'BPM', 'Task completed.'));
                }

                var task = me.getModel('Bpm.model.task.Task');
                task.load(openTaskRecord.get('id'), {
                    success: function (taskRecord) {
                        me.loadAssigneeForm(taskRecord);
                        me.loadEditTaskForm(taskRecord);
                        me.loadAboutTaskForm(taskRecord);
                        me.loadJbpmForm(taskRecord);
                    }
                });

            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        taskExecutionForm.getForm().markInvalid(json.errors);
                    }
                }
            }
        })*/
    }
    /*

    loadBpmForm: function (taskRecord) {
        var me = this,
            flowUrl;

        if (!me.getFormContainer()) {
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
*/
});