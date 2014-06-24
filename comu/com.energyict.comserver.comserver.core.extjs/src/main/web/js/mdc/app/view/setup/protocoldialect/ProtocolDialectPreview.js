Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.protocolDialectPreview',
    itemId: 'protocolDialectPreview',
    requires: [
        'Mdc.model.ProtocolDialect',
        'Mdc.view.setup.property.PropertyView',
        'Mdc.view.setup.protocoldialect.ProtocolDialectActionMenu'
    ],
    frame: true,
    title: "Details",

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'protocol-dialect-action-menu'
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
            itemId: 'protocolDialectPreviewForm',
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
                    xtype: 'propertyView'
                }

            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});



