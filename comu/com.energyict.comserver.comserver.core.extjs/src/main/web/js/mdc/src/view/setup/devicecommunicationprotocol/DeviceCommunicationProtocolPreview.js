Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolPreview',
    frame: true,
    border: true,
    itemId: 'deviceCommunicationProtocolPreview',

    requires: [
        'Uni.property.form.Property',
        'Mdc.model.DeviceCommunicationProtocol',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('deviceCommunicationProtocol.previewTitle', 'MDC', 'Selected protocol preview'),

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            privileges: Mdc.privileges.Communication.admin,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-communication-protocol-action-menu'
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
                                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
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
                        xtype: 'component',
                        html: '<h3 style="width: 238px; text-align: right;">' +
                            Uni.I18n.translate('deviceCommunicationProtocol.communicationProtocolDetails', 'MDC', 'Communication protocol details') +
                            '</h3>'
                    },
                    {
                        xtype: 'property-form',
                        isEdit: false,
                        layout: 'column',
                        frame: false,
                        defaults: {
                            xtype: 'container',
                            layout: 'form',
                            resetButtonHidden: true,
                            labelWidth: 250,
                            columnWidth: 0.5
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});