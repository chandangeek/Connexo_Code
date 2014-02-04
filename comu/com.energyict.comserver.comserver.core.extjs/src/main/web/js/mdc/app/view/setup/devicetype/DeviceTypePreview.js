Ext.define('Mdc.view.setup.devicetype.DeviceTypePreview', {
    extend: 'Ext.panel.Panel',
    border: false,
    margins: '0 10 10 10',
    alias: 'widget.deviceTypePreview',
    itemId: 'deviceTypePreview',
    hidden: true,
    requires: [
        'Mdc.model.DeviceType'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    tbar: [
        {
            xtype: 'component',
            html: '<h4>Device type</h4>',
            itemId: 'deviceTypePreviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: 'Actions',
            menu:{
                items:[
                    {
                        text: 'Edit',
                        itemId: 'editDeviceType',
                        action: 'editDeviceType'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: 'Delete',
                        itemId: 'deleteDeviceType',
                        action: 'deleteDeviceType'

                    }
                ]
            }
        }],

    items: [
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceTypePreviewForm',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
//                        align: 'stretch'
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
                                    fieldLabel: 'Name:',
                                    labelAlign: 'right',
                                    labelWidth:	150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'communicationProtocolName',
                                    fieldLabel: 'Device Communication protocol:',
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
                                    fieldLabel: 'canBeGateway:',
                                    readOnly: true,
                                    labelAlign: 'right',
                                    labelWidth:	150 },
                                {
                                    xtype: 'checkboxfield',
                                    name: 'isDirectlyAddressable',
                                    fieldLabel: 'isDirectlyAddressable:',
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
                                    fieldLabel: 'Data sources:',
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
                                    fieldLabel: 'deviceConfigurationCount:',
                                    renderer: function(item,b){
                                        return '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#' + item + '">'+ item  +' device configurations</a>';
                                    },
                                    labelAlign: 'right',
                                    labelWidth:	150 }
                            ]
                        }

                    ]
                },
                {
                    xtype: 'toolbar',
                    docked: 'bottom',
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            itemId: 'deviceTypeDetailsLink',
                            html: '' // filled in in Controller
                        }

                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

