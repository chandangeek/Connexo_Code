Ext.define('Bpm.controller.OpenTask', {
    extend: 'Ext.app.Controller',

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
            ref: 'formContent2',
            selector: 'bpm-task-open-task #formContent2'
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
            'bpm-task-open-task #btn-claim':{
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-release':{
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-start':{
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-save':{
                click: this.chooseAction
            },
            'bpm-task-open-task #btn-complete':{
                click: this.chooseAction
            }

        });

        listener = function dolisten(event){
            var me = this;

           
        };
        if (window.addEventListener){
            addEventListener("message", listener, false)
        } else {
            attachEvent("onmessage", listener)
        }
    },


    showOpenTask : function(taskId){
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/taksmanagementtasks'),
            openTaskView, openTaskForm, taskRecord,
            flowUrl;

        queryParams = {};

        sort = router.arguments.sort;
        user = router.arguments.user;
        dueDate = router.arguments.dueDate;
        status = router.arguments.status;
        process = router.arguments.process;

        sort && (sort != '') && (queryParams.sort = sort);
        user && (user != '') && (queryParams.user = user);
        dueDate && (dueDate != '') && (queryParams.dueDate = dueDate);
        status && (status != '') && (queryParams.status = status);
        process && (process != '') && (queryParams.process = process);
        tasksRoute.params.use = true;

        var task = me.getModel('Bpm.model.task.Task');
        task.load(taskId, {
            success: function (taskRecord) {

                openTaskView = Ext.create('Bpm.view.task.OpenTask', {
                //    returnLink: router.getRoute('workspace/taksmanagementtasks').buildUrl({}, queryParams),
                    taskRecord: taskRecord});

                //openTaskView.setLoading();
                //openTaskView.taskRecord = taskRecord;
                me.getApplication().fireEvent('openTask', taskRecord);

                openTaskForm = openTaskView.down('#frm-open-task');
                openTaskForm.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.openTaskTitle', 'BPM', "'{0}' task"), taskRecord.get('name')));

                //openTaskForm.loadRecord(taskRecord);
                me.getApplication().fireEvent('changecontentevent', openTaskView);
                openTaskView.setLoading();
                var rowIndex = Uni.store.Apps.findExact('name', 'Flow');
                if (rowIndex != -1){
                    flowUrl = Uni.store.Apps.getAt(rowIndex).get('url');
                    Ext.Ajax.request({
                        url: flowUrl + '/rest/task/' + taskId +'/showTaskForm',
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

                                    if (status == 'SUCCESS') {
                                        var formURL = xmlDoc.getElementsByTagName("formUrl");
                                        if (formURL && formURL.length > 0 && formURL[0].childNodes.length > 0) {
                                            openTaskView.formURL = formURL[0].childNodes[0].nodeValue;
                                            var html = "<iframe id='iframeId' src='" + openTaskView.formURL + "' frameborder='0' style='width:100%; height:100%'></iframe>";

                                            var formContent = me.getFormContent();
                                            formContent.getEl().dom.innerHTML = html;

                                            document.getElementById("iframeId").addEventListener('load', function() {
                                                openTaskView.setLoading(false);
                                            });
                                            //me.resizeReportPanel(formContent);
                                            me.refreshButtons(taskRecord);

                                        }
                                    }

                                }

                            } catch (err) {
                                openTaskView.setLoading(false);
                            }
                        }
                    })

                }

            },
            failure: function (record, operation) {
            }
        });
    },

     getXMLDoc: function(xml) {
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

    resizeReportPanel: function(component) {

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

    refreshButtons: function(taskRecord){
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

            openTaskForm = me.getOpenTaskPage().down('#frm-open-task-container').setLoading();
            var request = '{"action":"'+ action + '","taskId":"' + taskRecord.get('id') + '"}';
            frame.postMessage(request, me.getOpenTaskPage().formURL);
        }
    }

});