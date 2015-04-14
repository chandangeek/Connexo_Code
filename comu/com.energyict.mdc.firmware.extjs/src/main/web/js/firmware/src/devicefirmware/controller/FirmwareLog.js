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

    showDeviceFirmwareLog: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router');

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', 'device-firmware-log', {router: router, device: device});
                me.getLogsGrid().getSelectionModel().select(0);
            }
        });


    }

});
