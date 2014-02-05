Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceTypesSetup',
    autoScroll: true,
    itemId: 'deviceTypeSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',
//    border: 0,
//    region: 'center',

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items:[
                {
                    xtype: 'component',
                    html: '<h1>Device types</h1>',
                    margins: '10 10 10 10'
                },
                {
                    border: false,
                    tbar: [
                        '->',
                        {
                            text: 'Create device type',
                            itemId: 'createDeviceType',
                            xtype: 'button',
                            action: 'createDeviceType'
                        },
                        {
                            text: 'Bulk action',
                            itemId: 'deviceTypesBulkAction',
                            xtype: 'button'
                        }]
                },
                {
                    xtype: 'deviceTypesGrid'
                },
                {
                    xtype: 'component',
                    height : 50
                },
                {
                    xtype: 'deviceTypePreview'
                }
    ]}],


    initComponent: function () {
        this.callParent(arguments);
    }
});


