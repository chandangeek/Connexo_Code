Ext.define('Fwc.devicefirmware.view.LogPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-firmware-log-preview',

    requires: [
        'Fwc.devicefirmware.view.LogPreviewForm'
    ],

    items: {
        xtype: 'device-firmware-log-preview-form'
    }
});
