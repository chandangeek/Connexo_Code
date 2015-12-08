Ext.define('Dbp.deviceprocesses.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [],
    models: [
        'Dbp.deviceprocesses.model.StartProcess'
    ],
    stores: [
        'Dbp.deviceprocesses.store.AvailableProcesses'
    ],
    views: [
        'Dbp.deviceprocesses.view.SideMenu',
        'Dbp.deviceprocesses.view.DeviceProcessesMainView',
        'Dbp.deviceprocesses.view.StartProcess'
    ],
    refs: [
        {ref: 'mainPage', selector: 'dbp-device-processes-main-view'},
        {ref: 'openTasksDisplay', selector: '#dbp-preview-running-process-open-tasks'},
        {ref: 'startProcess', selector: 'dbp-start-processes'},
        {ref: 'processStartContent',selector: 'dbp-start-processes #process-start-content'},
    ],
    mRID: null,
    processRecord: null,

    init: function () {
        var me = this;
        me.control({

            '#process-start-content button[action=cancelStartProcess]': {
                click: this.cancelStartProcess
            },
            '#process-start-content button[action=startProcess]': {
                click: this.startProcess
            },
            'combobox[itemId=cbo-processes-definition]':
            {
                select: this.processComboChange
            }
        });
    },


    showStartProcess: function (mRID) {
        var me = this,
            processStore = Ext.getStore('Dbp.deviceprocesses.store.AvailableProcesses'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            processCombo;

        me.mRID = mRID;
        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = me.getStartProcess();

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);

                var jsonData;
                var request = {};
                me.getPrivileges();
                processStore.getProxy().setUrl(device.data.state.id);

                processStore.getProxy().extraParams = {privileges: Ext.encode(me.getPrivileges())};

                if (!widget) {
                    widget = Ext.widget('dbp-start-processes', {device: device});
                    me.getApplication().fireEvent('changecontentevent', widget);
                } else {
                    widget.device = device;
                }

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    getPrivileges: function () {
        var me = this,
        executionPrivileges =[];

        if (Dbp.privileges.DeviceProcesses.canExecuteLevel1()) {
            executionPrivileges.push({
                privilege: Dbp.privileges.DeviceProcesses.executeLevel1.toString()
            });
        }
        if (Dbp.privileges.DeviceProcesses.canExecuteLevel2()) {
            executionPrivileges.push({
                privilege: Dbp.privileges.DeviceProcesses.executeLevel2.toString()
            });
        }
        if (Dbp.privileges.DeviceProcesses.canExecuteLevel3()) {
            executionPrivileges.push({
                privilege: Dbp.privileges.DeviceProcesses.executeLevel3.toString()
            });
        }
        if (Dbp.privileges.DeviceProcesses.canExecuteLevel4()) {
            executionPrivileges.push({
                privilege: Dbp.privileges.DeviceProcesses.executeLevel4.toString()
            });
        }
        return executionPrivileges;

    },



    loadJbpmForm: function (processRecord) {
        var me = this,
            processStartContent = me.getProcessStartContent(),
            startProcess = me.getModel('Dbp.deviceprocesses.model.StartProcess'),
            propertyForm;

        if (processStartContent == undefined){
            return;
        }
        propertyForm = processStartContent.down('property-form');
        processStartContent.setLoading();

        me.processRecord = processRecord.lastSelection[0].data;

        startProcess.load(processRecord.lastValue,{
            success: function (startProcessRecord) {

                processStartContent.startProcessRecord = startProcessRecord;
                if (startProcessRecord && startProcessRecord.properties() && startProcessRecord.properties().count()) {
                    propertyForm.loadRecord(startProcessRecord);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }
                processStartContent.setLoading(false);
                propertyForm.up('#process-start-content').doLayout();
            },
            failure: function (record, operation) {
            }
        });

    },

    processComboChange: function (record) {
        var me = this;
        me.loadJbpmForm(record);
    },
    cancelStartProcess: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device').forward();
    },
    startProcess: function (button) {
        var me=this,
            processStartContent = me.getProcessStartContent(),
            router = this.getController('Uni.controller.history.Router'),
            startProcessRecord = processStartContent.startProcessRecord,
            //startProcess = me.getModel('Dbp.deviceprocesses.model.StartProcess'),
            propertyForm = processStartContent.down('property-form');

        propertyForm.updateRecord();
        startProcessRecord.beginEdit();
        startProcessRecord.set('mrid', me.mRID);
        startProcessRecord.set('deploymentId', me.processRecord.deploymentId);
        startProcessRecord.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dbp.startprocess.started', 'DBP', 'Process started.'));
                me.getController('Uni.controller.history.Router').getRoute('devices/device/processes').forward();
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        processStartContent.getForm().markInvalid(json.errors);
                    }
                }
            }
        })
    }
});