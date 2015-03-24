Ext.define('Dlc.devicelifecycles.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycles-preview',

    requires: [
        'Dlc.devicelifecycles.view.PreviewForm'
    ],

    items: {
        xtype: 'device-life-cycles-preview-form'
    }
});
