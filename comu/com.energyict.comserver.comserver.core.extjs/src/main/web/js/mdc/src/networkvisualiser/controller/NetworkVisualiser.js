Ext.define('Mdc.networkvisualiser.controller.NetworkVisualiser', {
    extend: 'Ext.app.Controller',
    models: [
        'Mdc.model.Device'
    ],
    stores: [
        'Uni.graphvisualiser.store.GraphStore',
        'Mdc.networkvisualiser.store.NetworkNodes',
        'Mdc.store.CommunicationTasksOfDevice'
    ],
    views: [
        'Uni.view.window.Confirmation',
        'Mdc.networkvisualiser.view.NetworkVisualiserView'
    ],
    refs: [

    ],

    init: function () {
        this.control({
            'visualiserpanel': {
                showretriggerwindow: this.showRetriggerWindow
            }
        });
    },

    showVisualiser: function(){
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.create('Mdc.networkvisualiser.view.NetworkVisualiserView',{router: router});
        widget.clearGraph();
        widget.store = Ext.getStore('Uni.graphvisualiser.store.GraphStore');
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showNetwork: function(deviceName) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.create('Mdc.networkvisualiser.view.NetworkVisualiserView', {router: router, yOffset: 45/*Due to the title*/}),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        widget.clearGraph();
        widget.store = Ext.getStore('Mdc.networkvisualiser.store.NetworkNodes');
        widget.store.getProxy().setUrl(deviceName);
        me.getApplication().fireEvent('changecontentevent', widget);
        //Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
        //    success: function (device) {
        //        me.getApplication().fireEvent('changecontentevent', widget);
        //        me.getApplication().fireEvent('loadDevice', device);
        //        viewport.setLoading(false);
        //    }
        //});
    },

    showRetriggerWindow: function(deviceName) {
        var me = this,
            communicationTasksOfDeviceStore = Ext.getStore('Mdc.store.CommunicationTasksOfDevice'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'mdc-retriggerCommunicationTasksWindow',
                deviceName: deviceName,
                green: true,
                confirmBtnUi: 'action',
                confirmText: Uni.I18n.translate('general.retrigger', 'MDC', 'Retrigger'),
                confirmation: function () {
                    me.retriggerCommTasks();
                }
            });

        communicationTasksOfDeviceStore.getProxy().setExtraParam('deviceId', deviceName);
        communicationTasksOfDeviceStore.load({
            callback: function () {
                confirmationWindow.insert(1, me.getRetriggerContent());
                confirmationWindow.show({
                    title: Uni.I18n.translate('general.retriggerCommTasksWindow.title', 'MDC', 'Retrigger communication tasks?')
                });
            }
        });
    },

    getRetriggerContent: function() {
        var communicationTasksOfDeviceStore = Ext.getStore('Mdc.store.CommunicationTasksOfDevice'),
            container = Ext.create('Ext.container.Container', {
                padding: '5 5 10 50',
                items: [
                    {
                        itemId: 'mdc-retriggerCommTaskWindow-infoMsg',
                        html: Uni.I18n.translate('general.retriggerCommTaskWindow.infoMsg', 'MDC', 'Failed communication tasks are selected by default'),
                        padding: '0 0 10 0'
                    },
                    {
                        itemId: 'mdc-retriggerCommTaskWindow-checkboxes-mainPanel',
                        layout: {
                            type: 'vbox'
                        },
                        items: []
                    }
                ]
            });

        communicationTasksOfDeviceStore.each(function(comTask) {
            var connectionDefinedOnDevice = comTask.get('connectionDefinedOnDevice'),
                isOnHold = comTask.get('isOnHold'),
                isSystemComtask = comTask.get('comTask').isSystemComTask;
            if (connectionDefinedOnDevice && !isOnHold && !isSystemComtask) {
                container.down('#mdc-retriggerCommTaskWindow-checkboxes-mainPanel').add({
                    xtype: 'checkbox',
                    itemId: 'mdc-retriggerCommTaskWindow-checkbox-' + comTask.get('comTask').id,
                    boxLabel: comTask.get('comTask').name,
                    taskId: comTask.get('comTask').id
                });
            }
        });
        return container;
    },

    destroyRetriggerWindow: function () {
        var retriggerWindow = Ext.ComponentQuery.query('#mdc-retriggerCommunicationTasksWindow')[0];
        if (retriggerWindow) {
            retriggerWindow.removeAll(true);
            retriggerWindow.destroy();
        }
    },

    retriggerCommTasks: function() {
        var me = this,
            communicationTasksOfDeviceStore = Ext.getStore('Mdc.store.CommunicationTasksOfDevice'),
            retriggerWindow = Ext.ComponentQuery.query('#mdc-retriggerCommunicationTasksWindow')[0],
            checkBoxOfComTask = undefined,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        retriggerWindow.setLoading();
        me.comTasks2Trigger = [];
        me.retriggerWindow = retriggerWindow;
        // Build up an array of the selected and hence to be triggered comtasks
        communicationTasksOfDeviceStore.each(function(comTask) {
            checkBoxOfComTask = retriggerWindow.down('#mdc-retriggerCommTaskWindow-checkbox-' + comTask.get('comTask').id);
            if (!Ext.isEmpty(checkBoxOfComTask) && checkBoxOfComTask.getValue()) {
                me.comTasks2Trigger.push(comTask);
            }
        });
        me.doRetriggerCommTasks({ scope: me });
    },

    doRetriggerCommTasks: function(context) {
        var me = context.scope;
        if (Ext.isEmpty(me.comTasks2Trigger)) {
            me.retriggerWindow.setLoading(false);
            me.destroyRetriggerWindow();
            return;
        }
        // When (re)triggering a commtask, some of the device's fields (eg. its version) are part of the payload.
        // Simply loading the device once and then retriggering the comtasks one by one didn't work, since after each successful trigger
        // the device's version changes and hence for the 2nd,3rd,... comtask trigger request you get an error telling you
        // that the device('s version) has been changed meanwhile (how we handle concurrent updates)
        // So, EACH TIME you have to reload the device first (to have its correct version) before you can perform the comtask trigger.
        Ext.ModelManager.getModel('Mdc.model.Device').load(me.retriggerWindow.deviceName, {
            success: function (device) {
                me.triggerComTask(me.comTasks2Trigger.pop(), device, me.doRetriggerCommTasks, context);
            }
        });
    },

    triggerComTask: function(comTask, device, callback, context) {
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(device.get('name')) + '/comtasks/' + comTask.get('comTask').id + '/runnow',
            method: 'PUT',
            params: '',
            isNotEdit: true,
            jsonData: {device: _.pick(device.getRecordData(), 'name', 'version', 'parent')},
            timeout: 180000,
            success: function() {
                if (Ext.isFunction(callback)) {
                    callback.call(context.scope, context);
                }
            }
        });
    }
});
