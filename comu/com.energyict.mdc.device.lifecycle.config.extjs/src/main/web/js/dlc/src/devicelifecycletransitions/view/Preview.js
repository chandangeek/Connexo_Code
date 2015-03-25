Ext.define('Dlc.devicelifecycletransitions.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycle-transitions-preview',

    requires: [
        'Dlc.devicelifecycletransitions.view.PreviewForm'
    ],

    items: {
        xtype: 'device-life-cycle-transitions-preview-form'
    }
});
