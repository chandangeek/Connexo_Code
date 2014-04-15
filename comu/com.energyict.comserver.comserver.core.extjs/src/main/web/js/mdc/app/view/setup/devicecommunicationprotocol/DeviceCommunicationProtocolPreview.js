Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.deviceCommunicationProtocolPreview',
    itemId: 'deviceCommunicationProtocolPreview',
    requires: [
        'Mdc.model.DeviceCommunicationProtocol',
        'Mdc.view.setup.property.PropertyView'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                border: false,
                padding: '0 10 0 10',
                tbar: [
                    {
                        xtype: 'component',
                        html: '<H4>' + Uni.I18n.translate('deviceCommunicationProtocol.noDeviceCommunicationProtocolSelected', 'MDC', 'No protocol selected') + '</H4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        height: '100px',
                        html: '<H5>' + Uni.I18n.translate('deviceCommunicationProtocol.selectDeviceCommunicationProtocol', 'MDC', 'Select a protocol to see its details') + '</H5>'
                    }
                ]

            },
            {
                xtype: 'form',
                border: false,
                itemId: 'deviceCommunicationProtocolPreviewForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('deviceCommunicationProtocol.previewTitle', 'MDC', 'Selected protocol preview') + '</h4>',
                        itemId: 'deviceCommunicationProtocolPreviewTitle'
                    },
                    '->',
                    {
                        icon: '../mdc/resources/images/gear-16x16.png',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        menu: {
                            items: [
                                {
                                    text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                    itemId: 'editProtocol',
                                    action: 'editProtocol'
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
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.50,
                                layout: {
                                    type: 'vbox'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.name', 'MDC', 'Name'),
                                        labelAlign: 'right',
                                        labelWidth: 250
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.50,
                                layout: {
                                    type: 'vbox'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceProtocolVersion',
                                        fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.version', 'MDC', 'Version'),
                                        labelAlign: 'right',
                                        labelWidth: 250
                                    }

                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'propertyView'
                    }

                ]
            }
        ]
        this.callParent(arguments);
    }
})
;

