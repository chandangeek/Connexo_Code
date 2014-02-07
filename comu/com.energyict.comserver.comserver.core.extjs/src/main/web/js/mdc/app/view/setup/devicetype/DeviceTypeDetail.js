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
                itemId: 'deviceTypeDetailForm',
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
                    },
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: 'Delete',
                        itemId: 'deleteButtonFromDetails',
                        action: 'deleteDeviceType'
                    },
                    {
                        xtype: 'button',
                        text: 'Edit',
                        itemId: 'editButtonFromDetails',
                        action: 'editDeviceType'
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
                                defaults:{
                                    labelWidth: 170
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: 'Name'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'communicationProtocolName',
                                        fieldLabel: 'Device Communication protocol'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceFunction',
                                        fieldLabel: 'deviceFunction'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'canBeGateway',
                                        fieldLabel: 'canBeGateway',
                                        renderer: function (item) {
                                            return item?'Yes':'No';
                                        },
                                        readOnly: true
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'canBeDirectlyAddressable',
                                        fieldLabel: 'canBeDirectlyAddressable',
                                        renderer: function (item) {
                                            return item?'Yes':'No';
                                        },
                                        readOnly: true
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
                                defaults:{
                                    labelWidth: 170
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'registerCount',
                                        fieldLabel: 'Data sources',
                                        renderer: function(item){
                                            return '<a href="#' + item + '">'+ item  +' registers</a>';
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'loadProfileCount',
                                        fieldLabel: ' ',
                                        renderer: function(item){
                                            return '<a href="#' + item + '">'+ item  +' loadprofiles</a>';
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'logBookCount',
                                        fieldLabel: ' ',
                                        renderer: function(item){
                                            return '<a href="#' + item + '">'+ item  +' logbooks</a>';
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceConfigurationCount',
                                        fieldLabel: 'deviceConfigurationCount',
                                        renderer: function(item){
                                            return '<a href="#' + item + '">'+ item  +' device configurations</a>';
                                        }
                                    }
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


