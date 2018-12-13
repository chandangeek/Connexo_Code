Ext.define('Mdc.networkvisualiser.controller.NetworkVisualiser', {
    extend: 'Ext.app.Controller',
    models: [
        'Mdc.model.Device'
    ],
    stores: [
        'Uni.graphvisualiser.store.GraphStore',
        'Mdc.networkvisualiser.store.NetworkNodes',
        'Mdc.store.CommunicationTasksOfDevice',
        'Mdc.networkvisualiser.store.DeviceSummary'
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
                showretriggerwindow: this.showRetriggerWindow,
                showdevicesummary: this.showDeviceSummary
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
        widget.store.getProxy().setExtraParam('filter', Ext.encode([
            {
                property: 'layers',
                value: [] /*No layers by default*/
            },
            {
                property: 'refresh',
                value: true
            }
        ]));
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
           success: function (device) {
               me.getApplication().fireEvent('changecontentevent', widget);
               widget.deviceId2Select = device.get('id');
               me.getApplication().fireEvent('loadDevice', device);
               viewport.setLoading(false);
           }
        });
    },

    showRetriggerWindow: function(deviceName) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'mdc-retriggerCommunicationTasksWindow',
                deviceName: deviceName,
                green: true,
                confirmBtnUi: 'action',
                confirmText: Uni.I18n.translate('general.retrigger', 'MDC', 'Retrigger'),
                confirmation: function () {
                    me.retriggerCommTasks();
                }
            }),
            deviceSummaryStore = this.getStore('Mdc.networkvisualiser.store.DeviceSummary');

        deviceSummaryStore.getProxy().setUrl(encodeURIComponent(deviceName));
        deviceSummaryStore.load(function(records) {
            confirmationWindow.insert(1, me.getRetriggerContent(records[0].getData().failedComTasks));
            confirmationWindow.show({
                title: Uni.I18n.translate('general.retriggerCommTasksWindow.title', 'MDC', 'Retrigger communication tasks?')
            });
        });
    },

    getRetriggerContent: function(failedComTasks) {
        var communicationTasksOfDeviceStore = Ext.getStore('Mdc.store.CommunicationTasksOfDevice'),
            // The loading of this store already happened in NetworkVisualiserView.preprocessMenuItemsBeforeShowing()
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
                    checked: Ext.Array.contains(failedComTasks, comTask.get('comTask').name),
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
            checkBoxOfComTask = undefined;

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
        var me = context.scope,
            performAfterRetrigger = function() {
                me.retriggerWindow.setLoading(false);
                me.destroyRetriggerWindow();
            };

        Ext.ModelManager.getModel('Mdc.model.Device').load(me.retriggerWindow.deviceName, {
            success: function (device) {
                me.triggerComTasks(device, performAfterRetrigger, context);
            }
        });
    },

    triggerComTasks: function(device, callback, context) {
        var me = context.scope,
            comTaskIds2Trigger = [];

        Ext.Array.each(me.comTasks2Trigger, function(comTask) {
            comTaskIds2Trigger.push(comTask.get('comTask').id);
        });

        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(device.get('name')) + '/comtasks/runnow',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                comTaskIds: comTaskIds2Trigger,
                device: _.pick(device.getRecordData(), 'name', 'version', 'parent')
            },
            timeout: 180000,
            success: function() {
                if (Ext.isFunction(callback)) {
                    callback.call(context.scope, context);
                }
            }
        });
    },

    showDeviceSummary: function(graphData) {
        var me = this,
            deviceSummaryStore = me.getStore('Mdc.networkvisualiser.store.DeviceSummary'),
            propertyViewer = Ext.ComponentQuery.query('#uni-property-viewer')[0],
            propertiesToDisplay = {},
            orderOfProperties = [
                'name',
                'serialNumber',
                'deviceType',
                'deviceConfiguration',
                'parent',
                'hopLevel',
                'descendants',
                'alarms',
                'issues',
                'failedComTasks',
                'nodeAddress',
                'state',
                'modulation',
                'phaseInfo',
                'linkQualityIndicator',
                'linkCost',
                'roundTrip'
            ],
            deviceProperties = [
                'name',
                'serialNumber',
                'deviceType',
                'deviceConfiguration',
                'alarms',
                'issues',
                'failedComTasks',
                'nodeAddress',
                'state',
                'modulation',
                'phaseInfo',
                'linkQualityIndicator',
                'linkCost',
                'roundTrip'
            ],
            fieldLabels = [
                Uni.I18n.translate('general.name', 'MDC', 'Name'),
                Uni.I18n.translate('general.serialNumber', 'MDC', 'Serial number'),
                Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                Uni.I18n.translate('general.parent', 'MDC', 'Parent'),
                Uni.I18n.translate('general.numberOfHops', 'MDC', 'Number of hops'),
                Uni.I18n.translate('general.totalDescendants', 'MDC', 'Total descendants'),
                Uni.I18n.translate('general.alarms', 'MDC', 'Alarms'),
                Uni.I18n.translate('general.issues', 'MDC', 'Issues'),
                Uni.I18n.translate('general.failedCommunicationTasks', 'MDC', 'Failed communication tasks'),
                Uni.I18n.translate('general.nodeAddress', 'MDC', 'Node address'),
                Uni.I18n.translate('general.associationState', 'MDC', 'Association state'),
                Uni.I18n.translate('general.modulation', 'MDC', 'Modulation'),
                Uni.I18n.translate('general.phaseInfo', 'MDC', 'Phase info'),
                Uni.I18n.translate('general.linkQualityIndicator', 'MDC', 'Link quality'),
                Uni.I18n.translate('general.linkCost', 'MDC', 'Link cost'),
                Uni.I18n.translate('general.roundTrip', 'MDC', 'Round trip')
            ];

        if (!propertyViewer.getCollapsed()) {
            propertyViewer.setLoading();
        }
        // 1. Query the device summary data
        deviceSummaryStore.getProxy().setUrl(encodeURIComponent(graphData.name));
        deviceSummaryStore.load(function(records) {
            var dataObject = records[0].getData(),
                pathPeriod = dataObject['period'];
            Ext.Array.each(deviceProperties, function(propertyName) {
                graphData[propertyName] = dataObject[propertyName];
            });

            for (var property in graphData) {
                if (graphData.hasOwnProperty(property)) {
                    var propertyIndex = orderOfProperties.indexOf(property),
                        fieldLabel = propertyIndex<0 ? undefined : fieldLabels[propertyIndex],
                        htmlEncode = true,
                        graphDataPropertyValue = graphData[property];

                    switch(property) {
                        case 'phaseInfo':
                            switch (graphDataPropertyValue) {
                                case 'INPHASE':
                                    graphDataPropertyValue = '0 &deg;';
                                    break;
                                case 'DEGREE60':
                                    graphDataPropertyValue = '60 &deg;';
                                    break;
                                case 'DEGREE120':
                                    graphDataPropertyValue = '120 &deg;';
                                    break;
                                case 'DEGREE180':
                                    graphDataPropertyValue = '180 &deg;';
                                    break;
                                case 'DEGREE240':
                                    graphDataPropertyValue = '240 &deg;';
                                    break;
                                case 'DEGREE300':
                                    graphDataPropertyValue = '300 &deg;';
                                    break;
                                case 'NOPHASEINFO':
                                    graphDataPropertyValue = Uni.I18n.translate('G3NodePLCInfo.nophaseinfo', 'MDC', 'No phase');
                                    break;
                                case 'UNKNOWN':
                                    graphDataPropertyValue = Uni.I18n.translate('G3NodePLCInfo.unknown', 'MDC', 'Unknown');
                                    break;
                            }
                            htmlEncode = false;
                            break;

                        case 'modulation':
                            graphDataPropertyValue = graphDataPropertyValue === 'UNKNOWN' ? Uni.I18n.translate('modulation.unknown', 'MDC', 'Unknown') : graphDataPropertyValue;
                            break;
                        case 'alarms':
                        case 'issues':
                            if (graphDataPropertyValue === 0) {
                                graphDataPropertyValue = undefined; // Display a zero value as a dash
                            }
                            break;
                        case 'failedComTasks':
                            if (Ext.isArray(graphDataPropertyValue)) {
                                if (graphDataPropertyValue.length > 1) {
                                    var formattedResult = '';
                                    Ext.Array.each(graphDataPropertyValue, function (value) {
                                        formattedResult += (value + '</br>');
                                    });
                                    graphDataPropertyValue = formattedResult;
                                    htmlEncode = false;
                                } else if (graphDataPropertyValue.length === 1) {
                                    graphDataPropertyValue = graphDataPropertyValue[0];
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    if (!Ext.isEmpty(fieldLabel)) {
                        propertiesToDisplay[fieldLabel] = {
                            value: graphDataPropertyValue,
                            htmlEncode: htmlEncode,
                            order: propertyIndex
                        };
                    }
                }
            }
            propertyViewer.setLoading(false);
            // 2. Display them
            var subTitle = Ext.isEmpty(pathPeriod) ? undefined
                : Uni.I18n.translate('general.pathLastUpdatedAtX', 'MDC', 'Path last updated at {0}', Uni.DateTime.formatDateTimeShort(new Date(pathPeriod.start)));
            propertyViewer.displayProperties(propertiesToDisplay, subTitle);
        });
    }
});
