Ext.define('Dlc.devicelifecyclestates.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycle-states-preview',

    requires: [
        'Dlc.devicelifecyclestates.view.PreviewForm'
    ],

    items: {
        xtype: 'device-life-cycle-states-preview-form'
    }
});
