Ext.define('Dlc.view.devicelifecycles.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycles-preview',

    requires: [
        'Dlc.view.devicelifecycles.PreviewForm'
    ],

    items: {
        xtype: 'device-life-cycles-preview-form'
    }
});
