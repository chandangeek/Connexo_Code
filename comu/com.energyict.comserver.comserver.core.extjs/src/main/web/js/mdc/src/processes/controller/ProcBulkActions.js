 /*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.controller.ProcBulkActions', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.processes.store.BulkChangeProcesses',
        'Bpm.startprocess.store.AvailableProcesses'
    ],

    requires: [
        'Mdc.processes.controller.ProcGlobalVars'
    ],


    models: [
        'Bpm.startprocess.model.ProcessContent'
    ],

    views: [
        'Mdc.processes.view.bulk.ProcessBulkBrowse',
        'Mdc.processes.view.RetryProcessDetails'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'process-bulk-browse'
        },
        {
            ref: 'bulkNavigation',
            selector: 'process-bulk-browse processses-bulk-navigation'
        }
    ],

    processDeploymentId: '',
    processId: '',
    processNameToStart: '',
    processObjectType: '',
    processVersionToStart: '',
    processDbVersionToStart: '',
    globalStartProcessRecord: null,
    actionIsAllowed: false,

    listeners: {
        retryRequest: function (wizard, failedItems) {
            //this.setFailedBulkRecordIssues(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },

    init: function () {
        this.control({
           'process-bulk-browse process-bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizarpreparationfinished: this.onWizardPreparationFinishedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent,
                wizaractionfinished: this.onBulkActionFinished
            },
            'process-bulk-browse process-bulk-wizard #confirmButton': {
                click: this.confirmClick
            }
        });
    },

    showBulkActions: function () {
        var me = this,
        processesStore = this.getStore('Mdc.processes.store.ProcessesBuffered'),
        queryStringValues = Uni.util.QueryString.getQueryStringValues(false),
        filter = [],
        widget, grid;
        var property;
        var value;

        var filterString = '';
        var mainProcessesStore = this.getStore('Mdc.processes.store.AllProcessesStore');
        var sortingFromMainStore = mainProcessesStore.getProxy().extraParams.sort;

        /* Assemble filter from query string */
        if (queryStringValues.process){
            property = "process";
            if (Ext.isArray(queryStringValues.process))
            {
                value = queryStringValues.process;
            }else{
                value = [queryStringValues.process];
            }
            filter.push({
                property: property,
                value: value
            });
        }

        if (queryStringValues.status){
            property = "status";
            if (Ext.isArray(queryStringValues.status))
            {
                value = queryStringValues.status;
            }else{
                value = [queryStringValues.status];
            }
            filter.push({
                property: property,
                value: value
            });
        }

        if (queryStringValues.variableId){
            property = "variableId";
            value = [queryStringValues.variableId];

            filter.push({
                property: property,
                value: value
            });

            if (queryStringValues.value){
                property = "value";
                value = [queryStringValues.value];

                filter.push({
                    property: property,
                    value: value
                });
            }
        }

        if (queryStringValues.user){
            property = "user";
            if (Ext.isArray(queryStringValues.user))
            {
                value = queryStringValues.user;
            }else{
                value = [queryStringValues.user];
            }
            filter.push({
                property: property,
                value: value
            });
        }

        if (queryStringValues.startedBetween){
            varValueString = queryStringValues.startedBetween.split('-');

            if (varValueString[0])
            filter.push({
                property: "startedOnFrom",
                value: parseInt(varValueString[0], 10)
            });

            if (varValueString[1])
            filter.push({
                property: "startedOnTo",
                value: parseInt(varValueString[1], 10)
            });
        }

        widget  = Ext.widget('process-bulk-browse');

        grid = widget.down('processes-bulk-step1').down('processes-selection-grid');
        grid.reconfigure(processesStore);
        grid.filterParams = Ext.clone(filter);

        me.getApplication().fireEvent('changecontentevent', widget);
        processesStore.data.clear();

        processesStore.clearFilter(true);

        /* Set sorting as in main process storage */
        processesStore.getProxy().setExtraParam('sort',sortingFromMainStore);
        processesStore.filter(filter);
        processesStore.load();


        processesStore.on('load', function () {
            grid.onSelectDefaultGroupType();
        }, me, {single: true});
    },

    /*setFailedBulkRecordIssues: function (failedIssues) {
        var record = this.getBulkRecord();/*,
            previousIssues = record.get('issues'),
            leftIssues = [];

        Ext.each(previousIssues, function (issue) {
            if (Ext.Array.contains(failedIssues, issue.get('id'))) {
                leftIssues.push(issue);
            }
        });

        record.set('issues', leftIssues);
        record.commit();*/
    //},

    onBulkActionEvent: function () {
        var widget = Ext.widget('bulk-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onWizardPrevEvent: function (wizard) {
        this.getBulkNavigation().movePrevStep();
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId();
        this.getBulkNavigation().moveNextStep();
        this.setBulkActionListActiveItem(wizard);
        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
    },

    onWizardStartedEvent: function (wizard) {
        this.createdWizard = wizard;
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardCancelledEvent: function () {
        Mdc.processes.controller.ProcGlobalVars.setDefaultParams = false;
        this.getController('Uni.controller.history.Router').getRoute('workspace/multisenseprocesses').forward(null,Uni.util.QueryString.getQueryStringValues(false));
    },

    setBulkActionListActiveItem: function (wizard) {
        var index = wizard.getActiveItemId();
    },

    processStep: function (func, wizard) {
        if (func in this) {
            this[func](wizard);
        }
    },

    getBulkRecord: function () {
          var bulkStore = Ext.getStore('Mdc.processes.store.BulkChangeProcesses'),
            bulkRecord = bulkStore.getAt(0);

        if (!bulkRecord) {
            bulkStore.add({
                operation: 'retry'
            });
        }
        return bulkStore.getAt(0);
    },

    /* Process cllick Next button on Step1 */
    processNextOnStep1: function (wizard) {
        var me = this;
        var record = this.getBulkRecord(),
            grid = wizard.down('processes-bulk-step1').down('processes-selection-grid'),
            selection = grid.getSelectionModel().getSelection(),
            name,version;

        if (grid.isAllSelected()) {
            var params = Ext.ComponentQuery.query('grid')[0].filterParams;
            record.set('allProcesses', true);
            record.set('params', params);
            record.set('processes', []);
            record.set('objectsToStartProcess', []);
        } else {
            record.set('processes', selection);
            record.set('allProcesses', false);
            record.set('params', []);
            record.set('objectsToStartProcess', []);
        }
        record.commit();

        /*Here we should check if action is allowed or not*/
        actionIsAllowed = true;
        if (grid.isAllSelected()) {
            /*go through all records in Mdc.processes.store.ProcessesBuffered*/
            var processesStore = me.getStore('Mdc.processes.store.ProcessesBuffered');

            name = processesStore.getAt(0).get('name');
            version = processesStore.getAt(0).get('version');
            var numberOfElements = processesStore.getCount();

            for (var i = 0;  i < numberOfElements; ++i) {
                if (processesStore.getAt(i).get('name') != name ){
                    actionIsAllowed = false;
                    break;
                }
                if (processesStore.getAt(i).get('version') != version ){
                    actionIsAllowed = false;
                    break;
                }
            }
        } else {
            if (selection.length == 1){
                actionIsAllowed = true;
            }else{
                name = selection[0].data.name;
                version = selection[0].data.version;
                for (var i = 1;  i < selection.length; ++i) {
                    if (selection[i].data.name != name ){
                        actionIsAllowed = false;
                        break;
                    }
                    if (selection[i].data.version != version ){
                        actionIsAllowed = false;
                        break;
                    }
                }
            }
        }

        var step2Panel = wizard.down('processes-bulk-step2');
        if (actionIsAllowed == false){
            step2Panel.down('#Restart').setValue(false);
            step2Panel.down('#Restart').disable();
            wizard.down('#next').disable();
        } else{
            step2Panel.down('#Restart').setValue(true);
            step2Panel.down('#Restart').enable();
            wizard.down('#next').enable();
        }

    },

    /* Process click Next button on Step2 */
    processNextOnStep2: function (wizard) {
        var me = this;
        var record = this.getBulkRecord(),
            step3Panel = wizard.down('processes-bulk-step3'),
            operation = record.get('operation'),
            view,
            widget;

        /*Here we have to load information about process*/
        switch (operation) {
            case 'retry':
                view = 'retry-process';
                widget = Ext.widget(view, {
                    labelWidth: 120,
                    controlsWidth: 500
                });
                break;
        }

        if (widget) {
            Ext.suspendLayouts();
            step3Panel.removeAll(true);
            Ext.resumeLayouts();
            step3Panel.add(widget);
        }


        /* Check if all processes from grid was selected or operator have chosen some specific processes */
        var record = this.getBulkRecord();
        var processName;
        if (record.get('allProcesses')){
            /* get process information get it from processBuffered */
            var processesStore = this.getStore('Mdc.processes.store.ProcessesBuffered');
            processNameToStart = processesStore.getAt(0).get('name');
            processObjectType = processesStore.getAt(0).get('type');
            processVersionToStart = processesStore.getAt(0).get('version');
        }else{
            var processes = record.get('processes');
            processNameToStart = processes[0].get('name');
            processObjectType = processes[0].get('type');
            processVersionToStart = processes[0].get('version');
        }
        /* Set name of selected process */
        widget.down('#processNameToRetry').setValue(processNameToStart);
        wizard.setLoading(true);

        /* This GET request is used to check if process is active and to obtain his versionDB. Instead of using two requests activeprocesses
        signal could be used. But it provides list of all active processes. In case if we have a lot of processes it will provide us
        a lot of unnecessary information */
        var processUrl = '/api/bpm/runtime/process/'+processNameToStart;
        Ext.Ajax.request({
            url: processUrl+'?version='+processVersionToStart,
            method: 'GET',
            timeout: 180000,
            async: false,
            success: function (response) {
                var decodedResponse = response.responseText ? Ext.decode(response.responseText, true) : null;
                processDbVersionToStart = decodedResponse.versionDB;
            },

            failure: function () {
                wizard.setLoading(false);
            }
        });

        /* This get request is used to obtain process DeploymentID and ProcessID */
        Ext.Ajax.request({
            url: processUrl,
            method: 'GET',
            timeout: 180000,
            async: false,
                success: function (response) {
                    var decodedResponse = response.responseText ? Ext.decode(response.responseText, true) : null;
                    processDeploymentId = decodedResponse.deploymentId;
                    processId = decodedResponse.processId;
                },

                failure: function () {
                    wizard.setLoading(false);
                }
            });

            /* Get process content and fill in property form */
             var processContent = me.getModel('Bpm.startprocess.model.ProcessContent');
             var propertyForm = step3Panel.down('retry-process').down('property-form');
             processContent.getProxy().setUrl(processDeploymentId);
             processContent.load(processId, {
                         success: function (startProcessRecord) {
                             globalStartProcessRecord = startProcessRecord;
                             if (startProcessRecord && startProcessRecord.properties() && startProcessRecord.properties().count()) {
                                 propertyForm.loadRecord(startProcessRecord);
                                 propertyForm.show();
                             } else {
                                 propertyForm.hide();
                             }

                             wizard.setLoading(false);
                         },
                         failure: function (record, operation) {
                            wizard.setLoading(false);
                         }
                     });
    },

    /* Process cllick Next button on Step3 */
    onWizardPreparationFinishedEvent: function (wizard) {
        me = this;
        var record = this.getBulkRecord(),
        step4Panel = wizard.down('processes-bulk-step4'),
        operation = record.get('operation'),
        message, widget;

        var step3Panel = wizard.down('processes-bulk-step3');
        var propertyForm = step3Panel.down('retry-process').down('property-form');

        if (propertyForm.isValid())
        {
            propertyForm.updateRecord();
            globalStartProcessRecord = propertyForm.getRecord();

            var processesStore = this.getStore('Mdc.processes.store.ProcessesBuffered');
            var filter = processesStore.getProxy().encodeFilters(processesStore.filters.getRange());

            /* Validation.Check number of process instances that can be retried  */
            me.validateProcessesAction(wizard, record.get('allProcesses'), filter);
        }
        /* In case if form is not valid we just do not anything and stay on card Step3. Error is shown for the invalid field */

    },

   validateProcessesAction: function (wizard, isAllDevices, filter) {
        var me = this,
            url = '/api/ddr/flowprocesses/validate',
            jsonData = {
                'processHistories': [],
            };
        var widget;
        var filterToValidate = "";
        var step4Panel = wizard.down('processes-bulk-step4');


        var record = me.getBulkRecord();

        if (!isAllDevices) {
            jsonData.processHistories = record.get('processes').map(function (value){
                    return {
                        processId: value.data.processId,
                        name: value.data.name,
                        startDate: value.data.startDate,
                        endDate: value.data.endDate,
                        status: value.data.status,
                        startedBy: value.data.startedBy,
                        duration: value.data.duration,
                        value: value.data.value,
                        version: value.data.version,
                        variableId: value.data.variableId,
                        objectName: value.data.objectName,
                        corrDeviceName: value.data.corrDeviceName,
                        issueType: value.data.issueType
                    }
                });
        } else {
            filterToValidate = filter;
        }

        wizard.setLoading(true);

        Ext.Ajax.request({
            url: url,
            method: 'POST',
            params: {filter: filterToValidate},
            jsonData: jsonData,
            timeout: 180000,
                callback: function (config, success, response) {
                    var decodedRsponse =  Ext.decode(response.responseText);
                    var msgText;

                    if (decodedRsponse.processHistories.length > 0)
                    {
                        var validatedProcesses = decodedRsponse.processHistories;
                        var tmpObjId;
                        var tmpObjType;
                        switch (validatedProcesses[0].variableId) {
                            case 'deviceId':
                                tmpObjId = 'deviceId';
                                tmpObjType = 'device';
                            break;
                            case 'alarmId':
                                tmpObjId = 'alarmId';
                                tmpObjType = 'alarm';
                            break;
                            case 'issueId':
                                tmpObjId = 'issueId';
                                tmpObjType = 'issue';
                            break;
                            }

                            var array =  validatedProcesses.map(function (value){
                                return {
                                    id: tmpObjId,
                                    type: tmpObjType,
                                    value: value.value
                                    }
                                });
                        record.set('objectsToStartProcess',array);

                        msgText = Uni.I18n.translate('processes.processesToRetry.validateOkTitle', 'MDC', '<h3>Retry selected process instances?</h3><br>') +
                                  Uni.I18n.translatePlural('processes.processesToRetry.validateOkMsg', decodedRsponse.processHistories.length, 'MDC', '-', '{0} process instances will be restarted', '{0} process instances will be restarted');


                        widget = Ext.widget('container', {
                                    style: 'margin: 20px 0',
                                    html: msgText
                                });
                                Ext.suspendLayouts();
                                step4Panel.removeAll(true);
                                Ext.resumeLayouts();
                                step4Panel.add(widget);




                        me.getBulkNavigation().moveNextStep();
                        me.setBulkActionListActiveItem(wizard);
                        wizard.getLayout().setActiveItem(++wizard.activeItemId);
                        wizard.fireEvent('wizardpagechange', wizard);

                    }else{

                        msgText = Uni.I18n.translate('processes.processesToRetry.validateNotOkMsg', 'MDC', 'No process instances can be retried');

                        widget = Ext.widget('container', {
                            style: 'margin: 20px 0',
                            html: msgText
                        });
                        Ext.suspendLayouts();
                        step4Panel.removeAll(true);
                        Ext.resumeLayouts();
                        step4Panel.add(widget);


                        me.getBulkNavigation().moveNextStep();
                        me.setBulkActionListActiveItem(wizard);
                        wizard.getLayout().setActiveItem(++wizard.activeItemId);
                        wizard.fireEvent('wizardpagechange', wizard);
                        wizard.down('#finish').disable();
                    }

                    wizard.setLoading(false);
                }
        });
    },


    /* Handle click Next button on Step3 */
    /* Here we send request to restart processes */
    onWizardFinishedEvent: function (wizard) {
        var me = this;
        var msgText;
        var step5Panel = wizard.down('processes-bulk-step5');

        wizard.setLoading(true);

        var record = this.getBulkRecord();

        var processJsonData = {
                    deploymentId: processDeploymentId,
                    id: processId,
                    processName: processNameToStart,
                    processVersion: processVersionToStart,
                    versionDB: processDbVersionToStart,//me.processRecord.versionDB,
                    properties: ( globalStartProcessRecord ? globalStartProcessRecord.getWriteData(true, true).properties : [] )
                },

        processUrl = '/api/bpm/runtime/processcontent/' + processDeploymentId  + '/' + processId;

        processJsonData.bulkBusinessObjects = record.get('objectsToStartProcess');

        Ext.Ajax.request({
                url: processUrl,
                method: 'PUT',
                jsonData: processJsonData,
                timeout: 180000,
                success: function (response) {
                    wizard.setLoading(false);

                    msgText = Uni.I18n.translate('processes.processesToRetry.retryResultOkTitle', 'MDC', '<h3>The task has been queued</h3><br>') +
                    Uni.I18n.translatePlural('processes.processesToRetry.retryResultOkMsg', record.get('objectsToStartProcess').length, 'MDC', '-', '{0} process instances have been triggered', '{0} process instances have been triggered')


                    widget = Ext.widget('container', {
                            style: 'margin: 20px 0',
                            html: msgText
                         });
                    Ext.suspendLayouts();
                    step5Panel.removeAll(true);
                    Ext.resumeLayouts();
                    step5Panel.add(widget);

                    me.getBulkNavigation().moveNextStep();
                },

                failure: function () {
                    wizard.setLoading(false);
                    me.getBulkNavigation().moveNextStep();
                }
        });

        me.setBulkActionListActiveItem(wizard);
    },

    onBulkActionFinished: function (wizard) {
        Mdc.processes.controller.ProcGlobalVars.setDefaultParams = false;
        this.getController('Uni.controller.history.Router').getRoute('workspace/multisenseprocesses').forward(null,Uni.util.QueryString.getQueryStringValues(false));
    },

    getIssueType: function (array, value) {
        for (var i = 0; i < array.length; i++) {
            if (array[i].id === value && array[i].title.indexOf('estimate') >= 0 ) {
                return "datavalidation";
            }
        }
        return "datacollection";
    }

});