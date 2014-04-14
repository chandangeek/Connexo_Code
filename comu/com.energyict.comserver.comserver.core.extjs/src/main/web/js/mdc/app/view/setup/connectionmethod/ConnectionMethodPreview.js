Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.connectionMethodPreview',
    itemId: 'connectionMethodPreview',
    requires: [
        'Mdc.model.ConnectionMethod'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'panel',
            border: false,
            padding: '0 10 0 10',
            tbar: [
                {
                    xtype: 'component',
                    html: '<H4>'+Uni.I18n.translate('connectionmethod.noConnectionMethodSelected', 'MDC', 'No connection method selected')+'</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>'+Uni.I18n.translate('connectionmethod.selectConnectionMethod', 'MDC', 'Select a connection method to see its details')+'</H5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'connectionMethodPreviewForm',
            padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>Connection method</h4>',
                    itemId: 'connectionMethodPreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    menu: {
                        items: [
                            {
                                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                itemId: 'editConnectionMethod',
                                action: 'editConnectionMethod'

                            },
                            {
                                xtype: 'menuseparator'
                            },
                            {
                                text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                itemId: 'deleteConnectionMethod',
                                action: 'deleteConnectionMethod'

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
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'direction',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.direction', 'MDC', 'Direction')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'simultaneousConnectionsAllowed',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.simultaneousConnectionsAllowed', 'MDC', 'Simultaneous connections allowed')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'setAsDefault',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.setAsDefault', 'MDC', 'Set as default')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'portPool',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.portPool', 'MDC', 'Port pool')
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
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionType',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'host',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.host', 'MDC', 'Host')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'portNumber',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.portNumber', 'MDC', 'Port number')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionTimeout',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionTimeout', 'MDC', 'Connection timeout')
                                }
                            ]
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


