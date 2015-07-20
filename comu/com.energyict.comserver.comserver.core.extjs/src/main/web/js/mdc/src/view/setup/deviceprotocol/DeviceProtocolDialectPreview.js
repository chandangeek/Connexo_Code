Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceProtocolDialectPreview',
    itemId: 'deviceProtocolDialectPreview',
    requires: [
        'Mdc.model.ProtocolDialect',
        'Uni.property.form.Property',
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu'
    ],
    frame: true,
    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.protocolDialectsActions,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-protocol-dialect-action-menu'
            }
        }
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
                    html: '<h4>' + Uni.I18n.translate('protocolDialect.noProtocolDialectSelected', 'MDC', 'No protocol selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('protocolDialect.selectProtocolDialect', 'MDC', 'Select a protocol to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            itemId: 'deviceProtocolDialectPreviewForm',
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
                                    fieldLabel: Uni.I18n.translate('protocolDialect.name', 'MDC', 'Name'),
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
                                /* {
                                 xtype: 'displayfield',
                                 name: 'availableForUse',
                                 fieldLabel: Uni.I18n.translate('protocolDialect.availableForUse', 'MDC', 'Available for use'),
                                 labelAlign: 'right',
                                 labelWidth: 250
                                 }*/

                            ]
                        }
                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'protocolDialectsDetailsTitle',
                    hidden: true,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: '<h3>' + Uni.I18n.translate('protocolDialect.protocolDialectDetails', 'MDC', 'Protocol dialect details') + '</h3>',
                            text: ''
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',

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
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});