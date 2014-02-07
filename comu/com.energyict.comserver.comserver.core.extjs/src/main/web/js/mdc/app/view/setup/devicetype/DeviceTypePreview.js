Ext.define('Mdc.view.setup.devicetype.DeviceTypePreview', {
    extend: 'Ext.panel.Panel',
    border: false,
    margins: '0 10 10 10',
    alias: 'widget.deviceTypePreview',
    itemId: 'deviceTypePreview',
    requires: [
        'Mdc.model.DeviceType'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<H4>No device type selected</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>Select a device type to view its detail.</H5>'
                }
            ]

        },
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
                    html: '<h4>Device type</h4>',
                    itemId: 'deviceTypePreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: 'Actions',
                    menu: {
                        items: [
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
                }
            ],
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
//                        align: 'stretch'
                    },
                    padding: '10 0 0 0',
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
                                    fieldLabel: 'Name',
                                    itemId: 'deviceName'

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
                                    xtype: 'fieldcontainer',
                                    columnWidth: 0.5,
                                    fieldLabel: 'Data sources',
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    items: [
                                        {
                                            xtype: 'component',
                                            name: 'registerCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: 'Registers'
                                            },
                                            itemId: 'deviceTypeRegistersLink'
                                        },


                                        {
                                            xtype: 'component',
                                            name: 'loadProfileCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: 'loadprofiles'
                                            },
                                            itemId: 'deviceTypeLoadProfilesLink'

                                        },
                                        {
                                            xtype: 'component',
                                            name: 'logBookCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: 'logbooks'
                                            },
                                            itemId: 'deviceTypeLogBooksLink'
                                        }
                                    ]
                                },

                                {
                                    xtype: 'displayfield',
                                    name: 'deviceConfigurationCount',
                                    fieldLabel: 'deviceConfigurationCount',
                                    renderer: function (item, b) {
                                        return '<a href="#' + item + '">' + item + ' device configurations</a>';
                                    }
                                }
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

