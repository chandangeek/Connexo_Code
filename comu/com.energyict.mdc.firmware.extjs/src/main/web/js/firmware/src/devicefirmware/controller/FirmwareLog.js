/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.controller.FirmwareLog', {
    extend: 'Ext.app.Controller',

    views: [
        'Fwc.devicefirmware.view.Log'
    ],

    requires: [
        'Mdc.model.Device'
    ],

    stores: [
        'Fwc.devicefirmware.store.FirmwareLogs'
    ],

    refs: [
        {ref: 'logPage', selector: 'device-firmware-log'},
        {ref: 'logsGrid', selector: 'device-firmware-log-grid'}
    ],

    init: function () {
        this.control({
            'device-firmware-log device-firmware-log-grid': {
                select: this.showFirmwareLogPreview
            }
        });
    },

    showFirmwareLogPreview: function (selectionModel, record) {
        var me = this,
            page = me.getLogPage(),
            preview = page.down('device-firmware-log-preview'),
            previewForm = page.down('device-firmware-log-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(Uni.DateTime.formatDateTimeLong(record.get('timestamp')));
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showDeviceFirmwareLog: function (deviceId) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            queryParams = router.queryParams,
            model = Ext.ModelManager.getModel('Fwc.model.Firmware'),
            logGrid;

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                model.getProxy().setUrl(device.get('deviceTypeId'));
                me.getApplication().fireEvent('loadDevice', device);
                model.load(router.arguments.firmwareId, {
                    success: function(firmware) {
                        me.getApplication().fireEvent('loadFirmware', firmware);
                        me.getApplication().fireEvent('changecontentevent', 'device-firmware-log', {router: router, device: device, title: router.getRoute().getTitle()});
                        logGrid = me.getLogsGrid();
                        logGrid.getStore().getProxy().setParams(deviceId, queryParams.firmwareComTaskId, queryParams.firmwareComTaskSessionId);
                        logGrid.getStore().load(function() {
                            logGrid.getSelectionModel().select(0);
                        });
                    }
                });
            }
        });
    }
});
