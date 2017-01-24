Ext.define('Mdc.controller.setup.DeviceTopology', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.DeviceTopology'
    ],

    views: [
        'Mdc.view.setup.devicetopology.Setup'
    ],

    refs: [
        {ref: 'deviceTopology', selector: '#deviceTopologySetup'},
        {ref: 'topFilter', selector: '#deviceTopologySetup #mdc-view-setup-devicechannels-topologiestopfilter'}
    ],

    init: function () {
        this.control({
            '#deviceTopologySetup #mdc-topology-edit-master-btn': {
                click: this.onEditMasterClicked
            },
            '#deviceTopologySetup #mdc-topology-remove-master-btn': {
                click: this.onRemoveMasterClicked
            },
            '#deviceTopologySetup #mdc-topology-edit-master-cancel-btn': {
                click: this.onEditMasterCancel
            },
            '#deviceTopologySetup #mdc-topology-edit-master-save-btn': {
                click: this.onEditMasterSave
            }
        });
    },

    showTopologyView: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceTopologyStore = me.getStore('Mdc.store.DeviceTopology'),
            widget;

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                var gatewayType = record.get('gatewayType');

                if (gatewayType === 'LAN' || gatewayType === 'HAN' || !record.get('isDirectlyAddressed')) {
                    widget = Ext.widget('deviceTopologySetup', {device: record, router: router, hasgateway: (gatewayType === 'LAN' || gatewayType === 'HAN')});
                    me.getApplication().fireEvent('loadDevice', record);
                    me.getApplication().fireEvent('changecontentevent', widget);
                    if (gatewayType === 'LAN' || gatewayType === 'HAN') {
                        deviceTopologyStore.getProxy().setExtraParam('deviceId', record.get('name'));
                        deviceTopologyStore.load();
                    }
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                }
            }
        });
    },

    onEditMasterClicked: function() {
        this.getDeviceTopology().addMasterContainerEditItems();
    },

    onEditMasterCancel: function() {
        this.getDeviceTopology().addMasterContainerViewItems();
    },

    onEditMasterSave: function() {
        var me = this,
            masterCombo = me.getDeviceTopology().down('#mdc-topology-masterCandidatesCombo'),
            device = me.getDeviceTopology().device;

        if (device) {
            if (Ext.isEmpty(masterCombo.getValue())) {
                me.updateDevice(
                    {
                        masterDeviceId: null,
                        masterDeviceName: null,
                        acknowledgeMessage: Uni.I18n.translate('deviceCommunicationTopology.masterRemoved', 'MDC', 'Master removed')
                    }
                );
            } else {
                me.updateDevice(
                    {
                        masterDeviceId: masterCombo.getValue(),
                        masterDeviceName: masterCombo.getRawValue(),
                        acknowledgeMessage: Uni.I18n.translate('deviceCommunicationTopology.masterSaved', 'MDC', 'Master saved')
                    }
                );
            }
        } else {
            me.getDeviceTopology().addMasterContainerViewItems(); // fall back - should never be the case
        }
    },

    onRemoveMasterClicked: function() {
        var me = this,
            deviceTopologyContent = me.getDeviceTopology();

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('deviceCommunicationTopology.removeMasterConfirmation.title', 'MDC', "Remove '{0}' as master device?", deviceTopologyContent.device.get('masterDeviceName')),
            msg: Uni.I18n.translate('deviceCommunicationTopology.removeMasterConfirmation.message', 'MDC', "This device will no longer be the master of '{0}'", deviceTopologyContent.device.get('name'), false),
            fn: function (action) {
                if (action === 'confirm') {
                    me.updateDevice(
                        {
                            masterDeviceId: null,
                            masterDeviceName: null,
                            acknowledgeMessage: Uni.I18n.translate('deviceCommunicationTopology.masterRemoved', 'MDC', 'Master removed')
                        }
                    );
                }
            }
        });
    },

    updateDevice: function (data) {
        var me = this,
            deviceTopologyContent = me.getDeviceTopology();

        deviceTopologyContent.setLoading(true);
        deviceTopologyContent.device.set('masterDeviceId', data.masterDeviceId);
        deviceTopologyContent.device.set('masterDeviceName', data.masterDeviceName);
        deviceTopologyContent.device.save({
            isNotEdit: true,
            success: function (deviceData) {
                me.getApplication().fireEvent('acknowledge', data.acknowledgeMessage);
                Ext.ModelManager.getModel('Mdc.model.Device').load(deviceData.get('name'), {
                    success: function (device) {
                        deviceTopologyContent.addMasterContainerViewItems();
                    },
                    callback: function () {
                        deviceTopologyContent.setLoading(false);
                    }
                });
                return true;
            },
            failure: function(record, operation) {
                deviceTopologyContent.setLoading(false);
            }
        });
    }
});
