Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.protocolDialectPreview',
    itemId: 'protocolDialectPreview',
    requires: [
        'Mdc.model.ProtocolDialect',
        'Mdc.view.setup.property.PropertyView'
    ],
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
                    html: '<H4>' + Uni.I18n.translate('protocolDialect.noProtocolDialectSelected', 'MDC', 'No protocol selected') + '</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>' + Uni.I18n.translate('protocolDialect.selectProtocolDialect', 'MDC', 'Select a protocol to see its details') + '</H5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'protocolDialectPreviewForm',
            padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>Protocol</h4>',
                    itemId: 'protocolDialectPreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    menu: {
                        items: [
                            {
                                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                itemId: 'editProtocolDialect',
                                action: 'editProtocolDialect'

                            }//,
                            /*{
                                xtype: 'menuseparator'
                            },
                            {
                                text: Uni.I18n.translate('protocolDialects.notAvailableForUse', 'MDC', 'Not available for use'),
                                itemId: 'changeAvailability',
                                action: 'changeAvailability'
                            }*/
                        ]
                    }
                }
            ],
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
                    xtype: 'propertyView'
                }

            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});



