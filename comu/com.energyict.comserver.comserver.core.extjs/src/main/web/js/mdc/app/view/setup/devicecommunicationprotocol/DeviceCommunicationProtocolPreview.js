Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolPreview',
    frame: true,
    border: true,
    itemId: 'deviceCommunicationProtocolPreview',

    requires: [
        'Mdc.model.DeviceCommunicationProtocol',
        'Mdc.view.setup.property.PropertyView'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('deviceCommunicationProtocol.previewTitle', 'MDC', 'Selected protocol preview'),

    tools: [
        {
            xtype: 'button',
            // TODO Replace this icon below with an 'actions' ui.
            icon: '../mdc/resources/images/actionsDetail.png',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
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

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                border: false,
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('deviceCommunicationProtocol.noDeviceCommunicationProtocolSelected', 'MDC', 'No protocol selected') + '</h4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        html: '<h5>' + Uni.I18n.translate('deviceCommunicationProtocol.selectDeviceCommunicationProtocol', 'MDC', 'Select a protocol to see its details') + '</h5>'
                    }
                ]
            },
            {
                xtype: 'form',
                border: false,
                itemId: 'deviceCommunicationProtocolPreviewForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.49,
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
                                columnWidth: 0.49,
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
        ];

        this.callParent(arguments);
    }
});