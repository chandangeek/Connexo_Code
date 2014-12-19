Ext.define('Mdc.view.setup.devicelogbooks.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLogbooksPreview',
    itemId: 'deviceLogbooksPreview',
    requires: [
        'Mdc.view.setup.devicelogbooks.PreviewForm',
        'Mdc.view.setup.devicelogbooks.ActionMenu',
        'Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView'
    ],
    layout: 'fit',
    frame: true,

    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'deviceLogbooksPreviewForm'
        };
        me.callParent(arguments)
    }


});
