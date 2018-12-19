 /*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.controller.ProcBulkActions', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.processes.store.BulkChangeProcesses',
        'Bpm.startprocess.store.AvailableProcesses'
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

    dataCollectionActivated: false,
    dataValidationActivated: false,

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
            this.setFailedBulkRecordIssues(failedItems);
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
            issuesStoreProxy = processesStore.getProxy(),
            queryStringValues = Uni.util.QueryString.getQueryStringValues(false),
            filter = [],
            widget, grid;

        console.log("SHOW OVERVIEW FOR PROCESS BULK ACTION!!!!");
        var filterString = '';
        var mainProcessesStore = this.getStore('Mdc.processes.store.AllProcessesStore');

        console.log("queryStringValues=",queryStringValues);
        var property;
        var value;
        /* Assemble filter from query string */
        if (queryStringValues.process){
            property = "process";
            console.log("PROCESSES=",queryStringValues.process);
            if (Ext.isArray(queryStringValues.process))
            {
                value = queryStringValues.process;
                console.log("VALUE=",value);
            }else{
                value = [queryStringValues.process];
                console.log("VALUE=",value);
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
                console.log("VALUE=",value);
            }else{
                value = [queryStringValues.status];
                console.log("VALUE=",value);
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

            console.log("VALUE=",value);
            varValueString = queryStringValues.startedBetween.split('-');
            console.log("varValueString[0]",varValueString[0]);
            console.log("varValueString[1]",varValueString[1]);

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

        processesStore.getProxy().setUrl("deviceId,alarmId,issueId");
        processesStore.filter(filter);
        processesStore.load();


        processesStore.on('load', function () {
            grid.onSelectDefaultGroupType();
        }, me, {single: true});
    },

    setFailedBulkRecordIssues: function (failedIssues) {
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
    },

    onBulkActionEvent: function () {
        var widget = Ext.widget('bulk-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onWizardPrevEvent: function (wizard) {
        var index = wizard.getActiveItemId(),
            operation = this.getBulkRecord().get('operation'),
            isRetry = (operation == 'retrycomm') || (operation == 'retrycommnow') || (operation == 'retryconn');

        if (index == 2 && isRetry) {
            console.log("onWizardPrevEvent RETRY!!!!");
            Ext.suspendLayouts();
            wizard.getLayout().setActiveItem(--wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);
            this.getBulkNavigation().moveToStep(2);
            Ext.resumeLayouts(true);
        } else {
            console.log("onWizardPrevEvent DO NOT RETRY!!!!");
            this.getBulkNavigation().movePrevStep();
        }
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId(),
            operation = this.getBulkRecord().get('operation'),
            isRetry = (operation == 'retrycomm') || (operation == 'retrycommnow') || (operation == 'retryconn');

        console.log("Enter onWizardNextEvent!!!!");

        if (index == 2 && isRetry) {
            index++;
            Ext.suspendLayouts();
            wizard.getLayout().setActiveItem(++wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);
            console.log("onWizardNextEvent RETRY!!!!");
            this.getBulkNavigation().moveToStep(4);
            Ext.resumeLayouts(true);
        } else {
            console.log("onWizardNextEvent DO NOT RETRY!!!!");
            this.getBulkNavigation().moveNextStep();
        }
        this.setBulkActionListActiveItem(wizard);
        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
    },

    onWizardStartedEvent: function (wizard) {
        this.createdWizard = wizard;
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardCancelledEvent: function () {
        //this.getController('Uni.controller.history.Router').getRoute('workspace/issues').forward();
        console.log('onWizardCancelledEvent!!!!!!!!!');
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

            console.log("SELECTION MODEL = ",grid.getSelectionModel());
            console.log("selection=",selection);
            console.log("record=",record);


        if (grid.isAllSelected()) {
            console.log("IS ALL SELECTED!!!!");
            var params = Ext.ComponentQuery.query('grid')[0].filterParams;
            record.set('allProcesses', true);
            record.set('params', params);
            record.set('processes', []);
            record.set('objectsToStartProcess', []);
        } else {
            console.log("NOT  ALL SELECTED!!!!");
            record.set('processes', selection);
            record.set('allProcesses', false);
            record.set('params', []);
            record.set('objectsToStartProcess', []);
        }

        console.log("RECORD BEFORE COMMIT =",record);
        record.commit();
        console.log("RECORD AFTER COMMIT =",record);

        /*Here we should check if action is allowed or not*/
        actionIsAllowed = true;
        if (grid.isAllSelected()) {
            /*go through all records in Mdc.processes.store.ProcessesBuffered*/
            var processesStore = me.getStore('Mdc.processes.store.ProcessesBuffered');

            console.log("processesStore.data = ", processesStore.data);

            console.log("processesStore.data.items = ", processesStore.data.items);

            name = processesStore.getAt(0).get('name');
            version = processesStore.getAt(0).get('version');
            var numberOfElements = processesStore.getCount();
            console.log("Number of records = ", numberOfElements);

            console.log("name =",name);
            console.log("version =",version);

            for (var i = 0;  i < numberOfElements; ++i) {
                console.log("ITERATION=",i);
                if (processesStore.getAt(i).get('name') != name ){
                    console.log("SET FALSE DUE TO NAME!!!!");
                    actionIsAllowed = false;
                    break;
                }
                if (processesStore.getAt(i).get('version') != version ){
                    console.log("SET FALSE DUE TO VERSION!!!!");
                    actionIsAllowed = false;
                    break;
                }
            }
        } else {
            console.log("NOT  ALL SELECTED!!!!");
            console.log("SELECTION = ",selection);
            console.log("IS ARRAY =", Ext.isArray(selection));
            console.log("SIZE",selection.length);
            console.log("NAME = ",selection[0].data.name);
            if (selection.length == 1){
                actionIsAllowed = true;
            }else{
                name = selection[0].data.name;
                version = selection[0].data.version;
                for (var i = 1;  i < selection.length; ++i) {
                    console.log("ITERATION=",i);
                    if (selection[i].data.name != name ){
                        console.log("SET SELECTION FALSE DUE TO NAME!!!!");
                        actionIsAllowed = false;
                        break;
                    }
                    if (selection[i].data.version != version ){
                        console.log("SET SELECTION FALSE DUE TO VERSION!!!!");
                        actionIsAllowed = false;
                        break;
                    }
                }
            }
        }


        var step2Panel = wizard.down('processes-bulk-step2');
        console.log("step2Panel=",step2Panel)
        if (actionIsAllowed == false){
            step2Panel.down('#Restart').setValue(false);
            step2Panel.down('#Restart').disable();
            wizard.down('#next').disable();
        } else{
            console.log("ENABLE ACTION");
            step2Panel.down('#Restart').setValue(true);
            step2Panel.down('#Restart').enable();
            wizard.down('#next').enable();
        }

    },

    /* Process click Next button on Step2 */
    processNextOnStep2: function (wizard) {
        var me = this;
        var record = this.getBulkRecord(),
            //step3Panel = wizard.down('bulk-step3'),
            step3Panel = wizard.down('processes-bulk-step3'),
            operation = record.get('operation'),
            view,
            widget;

        console.log("STEP2 NEXT IS PRESSED for opertation=",operation);

        /*Here we have to load information about process*/

        switch (operation) {
            case 'retry':
                view = 'retry-process';
                console.log("CREATE retry-process form");
                widget = Ext.widget(view, {
                    labelWidth: 120,
                    controlsWidth: 500
                });
                break;
        }

/*        if (!Ext.isEmpty(widget.items.getAt(1))) {
            widget.items.getAt(1).margin = '0';
        }*/

        if (widget) {
            Ext.suspendLayouts();
            step3Panel.removeAll(true);
            Ext.resumeLayouts();
            step3Panel.add(widget);
        }


        /* Check if all processes from grid was selected or operator have chosen some specific processes */
        console.log("CHECK IF ALL PROCESSES ARE SELECTED !!!!!");
        var record = this.getBulkRecord();
        var processName;
        if (record.get('allProcesses')){
            /* get process information get it from processBuffered */
            console.log("ALL PROCESSES ARE SELECTED");
            var processesStore = this.getStore('Mdc.processes.store.ProcessesBuffered');
            processName = processesStore.getAt(0).get('name');
            processNameToStart = processesStore.getAt(0).get('name');
            processObjectType = processesStore.getAt(0).get('type');
            processVersionToStart = processesStore.getAt(0).get('version');
        }else{
        console.log("NOT ALL PROCESSES ARE SELECTED");
            var processes = record.get('processes');
            console.log('processes[0]',processes[0]);
            processName = processes[0].get('name');
            processNameToStart = processes[0].get('name');
            processObjectType = processes[0].get('type');
            processVersionToStart = processes[0].get('version');
        }

        console.log("processName = ",processName);

        /* Set name of selected process */
        widget.down('#processNameToRetry').setValue(processName);

        wizard.setLoading(true);

            /* This get request is used to check if process is active and to obtain his versionDB. Instead of using two requests activeprocesses
            signal could be used. But it provides list of all active processes. In case if we have a lot of processes it will provide us
            a lot of unnecessary information */
            console.log("GET VERSION of PROCESS!!!!");
            var processUrl = '/api/bpm/runtime/process/'+processName;
            Ext.Ajax.request({
                url: processUrl+'?version='+processVersionToStart,
                method: 'GET',
                timeout: 180000,
                async: false,
                success: function (response) {
                    var decodedResponse = response.responseText ? Ext.decode(response.responseText, true) : null;
                    console.log("decodedResponse = ",decodedResponse);
                    processDbVersionToStart = decodedResponse.versionDB;
                },

                failure: function () {
                    console.log("FAILED TO GET RESPONSE !!!!");
                    wizard.setLoading(false);
                }
            });





            /* This get request is used to obtain process DeploymentID and ProcessID */
            console.log("TRY TO GET INFORMATION ABOUT PROCESS!!!!!",processUrl);
            Ext.Ajax.request({
                url: processUrl,
                method: 'GET',
                timeout: 180000,
                async: false,
                success: function (response) {
                    console.log("GET RESPONSE response = ",response);
                    var decodedResponse = response.responseText ? Ext.decode(response.responseText, true) : null;

                    processDeploymentId = decodedResponse.deploymentId;
                    processId = decodedResponse.processId;

                    console.log("processDeploymentId=",processDeploymentId);
                    console.log("processID=",processId);

                    //wizard.setLoading(false);
                },

                failure: function () {
                    console.log("FAILED TO GET RESPONSE !!!!");
                    wizard.setLoading(false);
                }
            });

            /* GET PROCESS CONTENT HERE and fill in property form */
             var processContent = me.getModel('Bpm.startprocess.model.ProcessContent');
             var propertyForm = step3Panel.down('retry-process').down('property-form');
             processContent.getProxy().setUrl(processDeploymentId);
             processContent.load(processId, {
                         success: function (startProcessRecord) {
                             console.log("PROPERTIES WAS LOADED startProcessRecord=",startProcessRecord);


                             globalStartProcessRecord = startProcessRecord;
                             if (startProcessRecord && startProcessRecord.properties() && startProcessRecord.properties().count()) {
                                 console.log("SHOW FORM WITH =",startProcessRecord);
                                 propertyForm.loadRecord(startProcessRecord);
                                 propertyForm.show();
                             } else {
                                 console.log("Hide form");
                                 propertyForm.hide();
                             }

                             console.log("globalStartProcessRecord =",globalStartProcessRecord);

                             wizard.setLoading(false);
                             //propertyForm.up('#process-start-content').doLayout();
                         },
                         failure: function (record, operation) {
                            console.log("LOADING PROPERTIES IS FAILED");
                            wizard.setLoading(false);
                             /*startProcessPanel.setLoading(false);
                             propertyForm.hide();
                             propertyForm.up('#process-start-content').doLayout();*/
                         }
                     });


    },

    /* Process cllick Next button on Step3 */
    onWizardPreparationFinishedEvent: function (wizard) {
        me = this;
        var record = this.getBulkRecord(),
        step4Panel = wizard.down('processes-bulk-step4'),
        operation = record.get('operation'),
        //formPanel = wizard.down('processes-bulk-step3').down('form'),
        message, widget;

        var step3Panel = wizard.down('processes-bulk-step3');
        var propertyForm = step3Panel.down('retry-process').down('property-form');


        console.log("propertyForm is valid? = ",propertyForm.isValid());

        if (propertyForm.isValid())
        {
            console.log("PRINT PROPERTIES FOR PROCESS BEFORE UPDATE RECORD!!!!!");

            console.log("globalStartProcessRecord = ",globalStartProcessRecord);

            propertyForm.updateRecord();
            console.log('propertyForm.getRecord() = ',propertyForm.getRecord());

            console.log("PRINT PROPERTIES FOR PROCESS AFTER UPDATE RECORD!!!!!");

            globalStartProcessRecord = propertyForm.getRecord();

            console.log("globalStartProcessRecord = ",globalStartProcessRecord);

            if(globalStartProcessRecord)
            {
                console.log("PROPERTIES = ", globalStartProcessRecord.getWriteData(true, true).properties);
            }



            console.log("globalStartProcessRecord = ",globalStartProcessRecord);

            var processesStore = this.getStore('Mdc.processes.store.ProcessesBuffered');
            var filter = processesStore.getProxy().encodeFilters(processesStore.filters.getRange());

            console.log("FILTER TO SEND TO BE = ",filter);
            /* HERE SHOULD BE ADDED VALIDATION FROM BE */

            me.validateProcessesAction(wizard, record.get('allProcesses'), filter);

/*          me.getBulkNavigation().moveNextStep();
            me.setBulkActionListActiveItem(wizard);
            wizard.getLayout().setActiveItem(++wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);*/

        }
        /* In case if form is not valid we just do not anything and stay on card Step3. Error is shown for the invalid field */

    },
    /*xromvyu*/
   validateProcessesAction: function (wizard, isAllDevices, filter) {
        var me = this,
            url = '/api/ddr/flowprocesses/validate?variableid=deviceId,alarmId,issueId',//+'?filter='+filter,
            jsonData = {
                'processHistories': [],
            };
        var widget;
        var filterToValidate = "";
        var step4Panel = wizard.down('processes-bulk-step4');


        var record = me.getBulkRecord();

        if (!isAllDevices) {
            jsonData.processHistories = record.get('processes').map(function (value){
                    console.log("VALUE=",value.data.value);
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
                    console.log("response=",response);
                    console.log("Ext.decode(response.responseText)=", Ext.decode(response.responseText));
                    var decodedRsponse =  Ext.decode(response.responseText);
                    var msgText;

                    if (decodedRsponse.processHistories.length > 0)
                    {
                        var validatedProcesses = decodedRsponse.processHistories;

                        console.log("validatedProcesses=",validatedProcesses);
                        console.log("variableId=",validatedProcesses[0].variableId);
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

                            //record.set('objectsToStartProcess')
                            var array =  validatedProcesses.map(function (value){
                                console.log("VALUE=",value.value);
                                return {
                                    id: tmpObjId,
                                    type: tmpObjType,
                                    value: value.value
                                    }
                                });
                        record.set('objectsToStartProcess',array);

                        console.log("objectsToStartProcess = ",record.get('objectsToStartProcess'));

                        msgText= Uni.I18n.translate('processes.processesToRetry.retrymsg2', 'MDC', '<h3>Retry selected process instances?</h3><br>') +
                                 Uni.I18n.translatePlural('processes.processesToRetry.retrymsg1', decodedRsponse.processHistories.length, 'MDC', '-', '{0} process instances will be restarted', '{0} process instances will be restarted');


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

                        msgText = Uni.I18n.translate('processes.processesToRetry.retrymsgNotOk', 'MDC', 'No process instances can be retried');

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



    /* Process cllick Next button on Step3 */
    /* Here we send request to restart processes */
    onWizardFinishedEvent: function (wizard) {
       console.log("onWizardFinishedEvent!!!!!!!!!!!!!");
       console.log("SEND START REQUEST!!!", processDeploymentId);
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

            console.log('processJsonData=',processJsonData);



            console.log("Selected processes = ",record.get('objectsToStartProcess'));
            processJsonData.bulkBusinessObjects = record.get('objectsToStartProcess');

            console.log("START PROCESSES IN SEARCH !!!!!!!!!!!!!!!!!!!!!");
            console.log('processJsonData=',processJsonData);
            Ext.Ajax.request({
                url: processUrl,
                method: 'PUT',
                jsonData: processJsonData,
                timeout: 180000,
                success: function (response) {
                    console.log('process was started!!!!!!!!!!!');
                    wizard.setLoading(false);

                    msgText = Uni.I18n.translate('processes.processesToRetry.retryMsgResultOk', 'MDC', '<h3>The task has been queued</h3><br>') +
                    Uni.I18n.translatePlural('processes.processesToRetry.retrymsg1', record.get('objectsToStartProcess').length, 'MDC', '-', '{0} process instances have been triggered', '{0} process instances have been triggered')


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
                    console.log('failed to start process!!!!!!!!!!!');
                    wizard.setLoading(false);
                    me.getBulkNavigation().moveNextStep();
                }
            });

        me.setBulkActionListActiveItem(wizard);
    },


    onBulkActionFinished: function (wizard) {
        //Possibly we have to save here filter parameters and pass it to main page
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
