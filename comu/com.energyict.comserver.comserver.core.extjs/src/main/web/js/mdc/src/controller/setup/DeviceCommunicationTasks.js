Ext.define('Mdc.controller.setup.DeviceCommunicationTasks', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.store.DeviceSchedules',
        'Mdc.view.setup.devicecommunicationtask.ChangeConnectionItemPopUp'
    ],

    views: [
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskSetup',
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskGrid'
    ],

    stores: [
        'CommunicationTasksOfDevice',
        'ConnectionMethodsOfDevice',
        'ProtocolDialectsOfDevice'
    ],

    refs: [
        {ref: 'deviceCommunicationTaskGrid', selector: '#deviceCommunicationTaskGrid'},
        {ref: 'deviceCommunicationTaskPreviewForm', selector: '#deviceCommunicationTaskPreviewForm'},
        {ref: 'deviceCommunicationTaskPreview', selector: '#deviceCommunicationTaskPreview'},
        {ref: 'changeConnectionItemForm', selector: '#changeConnectionItemForm'}
    ],

    init: function () {
        this.control({
                '#deviceCommunicationTaskGrid': {
                    selectionchange: this.showDeviceCommunicationTaskPreview
                },
                '#device-communication-task-action-menu': {
                    show: this.configureMenu
                },
                '#changeFrequencyOfDeviceComTask[action=changeFrequencyOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#changeConnectionMethodOfDeviceComTask[action=changeConnectionMethodOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#changeProtocolDialectOfDeviceComTask[action=changeProtocolDialectOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#changeUrgencyOfDeviceComTask[action=changeUrgencyOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#runDeviceComTask[action=runDeviceComTask]': {
                    click: this.runDeviceComTask
                },
                '#changeButton[action=changeUrgencyOfDeviceComTask]':{
                    click: this.changeUrgency
                },
                '#changeButton[action=changeProtocolDialectOfDeviceComTask]':{
                    click: this.changeProtocolDialect
                },
                '#changeButton[action=changeFrequencyOfDeviceComTask]':{
                    click: this.changeFrequency
                },
                '#changeButton[action=changeConnectionMethodOfDeviceComTask]':{
                    click: this.changeConnectionMethod
                }
            }
        );
    },

    showDeviceCommunicationTasksView: function (mrid) {
        console.log('show device communication tasks --->' + mrid);
        var me = this;
        var communicationTasksOfDeviceStore = me.getCommunicationTasksOfDeviceStore();
        this.mrid = mrid;
        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                communicationTasksOfDeviceStore.getProxy().setExtraParam('mrid', mrid);
                communicationTasksOfDeviceStore.load({
                    callback: function () {
                        var widget = Ext.widget('deviceCommunicationTaskSetup', {mrid: mrid});
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getDeviceCommunicationTaskGrid().getSelectionModel().doSelect(0);
                    }
                });

            }
        });
    },

    configureMenu: function (menu) {
        //todo
//        var activate = menu.down('#activate'),
//            deactivate = menu.down('#deactivate'),
//            active = menu.record.data.active;
//        if (active) {
//            deactivate.show();
//            activate.hide();
//        } else {
//            activate.show();
//            deactivate.hide();
//        }
    },

    showDeviceCommunicationTaskPreview: function () {
        var deviceCommunicationTasks = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection();
        if (deviceCommunicationTasks.length == 1) {
            this.getDeviceCommunicationTaskPreviewForm().loadRecord(deviceCommunicationTasks[0]);
            var communicationTaskName = deviceCommunicationTasks[0].get('comTask').name;
            this.getDeviceCommunicationTaskPreview().getLayout().setActiveItem(1);
            this.getDeviceCommunicationTaskPreview().setTitle(communicationTaskName);
        } else {
            this.getConnectionMethodPreview().getLayout().setActiveItem(0);
        }
    },

    runDeviceComTask: function () {

    },

    showChangePopUp: function (menuItem) {
        var me=this;
        var comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        switch (menuItem.action) {
            case 'changeConnectionMethodOfDeviceComTask':
                var connectionMethodsOfDeviceStore = this.getConnectionMethodsOfDeviceStore();
                connectionMethodsOfDeviceStore.getProxy().setExtraParam('mrid', this.mrid);
                connectionMethodsOfDeviceStore.load({
                    callback: function(){
                        me.showPopUp(menuItem.action,connectionMethodsOfDeviceStore,comTask.get('connectionMethod'));
                    }
                });
                break;
            case 'changeFrequencyOfDeviceComTask':
                me.showPopUp(menuItem.action,null,comTask.get('temporalExpression'));
                break;
            case 'changeProtocolDialectOfDeviceComTask':
                var protocolDialectsOfDeviceStore = this.getProtocolDialectsOfDeviceStore();
                protocolDialectsOfDeviceStore.getProxy().setExtraParam('mRID', this.mrid);
                protocolDialectsOfDeviceStore.load({
                    callback: function(){
                        me.showPopUp(menuItem.action,protocolDialectsOfDeviceStore,comTask.get('protocolDialect'));
                    }
                });
                break;
            case 'changeUrgencyOfDeviceComTask':
                me.showPopUp(menuItem.action,null,comTask.get('urgency'));
                break;
            case 'runDeviceComTask':
                break;
        }
    },

    showPopUp:function(action,store,initialValue){
        var widget = Ext.widget('changeConnectionItemPopUp', {action: action,store:store,init: initialValue});
        var comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.comTask = comTask.get('comTask');
       // widget.down('#changeConnectionItemForm').loadRecord(comTask);
        widget.show();
    },

    changeConnectionMethod: function(){
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.connectionMethod = values.name;
        this.sendToServer(request);
    },

    changeUrgency: function(){
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.urgency = values.urgency;
        this.sendToServer(request);
    },

    changeFrequency: function(){
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.temporalExpression = values.schedule;
        this.sendToServer(request);
    },

    changeProtocolDialect: function(){
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.protocolDialect = values.name;
        this.sendToServer(request);
    },

    sendToServer: function(request){
        request.comTask = this.comTask;
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/'+ this.mrid + '/comtasks',
            method: 'PUT',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                location.href = '#/devices/' + me.mrid + '/comtasks';
            }
        });
    }
});