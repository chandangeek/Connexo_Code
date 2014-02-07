Ext.define('Mdc.view.setup.devicetype.DeviceTypeDetail', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceTypeDetail',
    itemId: 'deviceTypeDetail',
    autoScroll: true,
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
                xtype: 'form',
                border: false,
                itemId: 'deviceTypePreviewForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>Overview</h4>',
                        itemId: 'deviceTypePreviewTitle'
                    }
                ],

                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: 'Name',
                                        labelAlign: 'right',
                                        labelWidth:	150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'communicationProtocolName',
                                        fieldLabel: 'Device Communication protocol',
                                        labelAlign: 'right',
                                        labelWidth:	150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceFunction',
                                        fieldLabel: 'deviceFunction',
                                        labelAlign: 'right',
                                        labelWidth:	150
                                    },
                                    {
                                        xtype: 'checkboxfield',
                                        name: 'canBeGateway',
                                        fieldLabel: 'canBeGateway',
                                        readOnly: true,
                                        labelAlign: 'right',
                                        labelWidth:	150 },
                                    {
                                        xtype: 'checkboxfield',
                                        name: 'canBeDirectlyAddressable',
                                        fieldLabel: 'canBeDirectlyAddressable',
                                        readOnly: true,
                                        labelAlign: 'right',
                                        labelWidth:	150
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'registerCount',
                                        fieldLabel: 'Data sources',
                                        renderer: function(item,b){
                                            return '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#' + item + '">'+ item  +' registers</a>';
                                        },
                                        labelAlign: 'right',
                                        labelWidth:	150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'loadProfileCount',
                                        fieldLabel: ' ',
                                        renderer: function(item,b){
                                            return '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#' + item + '">'+ item  +' loadprofiles</a>';
                                        },
                                        labelAlign: 'right',
                                        labelWidth:	150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'logBookCount',
                                        fieldLabel: ' ',
                                        renderer: function(item,b){
                                            return '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#' + item + '">'+ item  +' logbooks</a>';
                                        },
                                        labelAlign: 'right',
                                        labelWidth:	150 },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceConfigurationCount',
                                        fieldLabel: 'deviceConfigurationCount',
                                        renderer: function(item,b){
                                            return '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#' + item + '">'+ item  +' device configurations</a>';
                                        },
                                        labelAlign: 'right',
                                        labelWidth:	150 }
                                ]
                            }

                        ]
                    }
                ]
            }

        ]}],


    initComponent: function () {
        this.callParent(arguments);
    }
});


