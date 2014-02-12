Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',
    requires: [
        'Mdc.model.RegisterMapping'
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
                    html: '<H4>' + I18n.translate('registermapping.noRegisterMappingSelected', 'MDC', 'No register type selected') + '</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>' + I18n.translate('registermapping.selectRegisterMapping', 'MDC', 'Select a register type to see its details') + '</H5>'
                }
            ]

        },


        {
            xtype: 'form',
            border: false,
            itemId: 'registerMappingPreviewForm',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>Selected register preview</h4>',
                        itemId: 'registerMappingPreviewTitle'
                    },
                    '->',
                    {
                        icon: 'resources/images/gear-16x16.png',
                        text: 'Actions',
                        menu: {
                            items: [
                                {
                                    text: 'Remove',
                                    itemId: 'deleteRegisterMapping',
                                    action: 'deleteRegisterMapping'

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
                                    fieldLabel: 'Name',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'registerIdentifier',
                                    fieldLabel: 'Reading type',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'obisCode',
                                    fieldLabel: 'Obis Code',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'dataCollectionGroup',
                                    fieldLabel: 'Data collection group',
                                    labelAlign: 'right',
                                    labelWidth: 150
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
                                    name: 'overFlowValue',
                                    fieldLabel: 'Overflow value',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'numberOfFractionDigits',
                                    fieldLabel: 'Number of fraction digits',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'multiplier',
                                    fieldLabel: 'Multiplier',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'multiplierMode',
                                    fieldLabel: 'Multiplier mode',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'linkedToChannelAs',
                                    fieldLabel: 'Linked to channel as',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                }
                            ]
                        }
                    ]
                }/*,
                {
                    xtype: 'toolbar',
                    docked: 'bottom',
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            itemId: 'registerMappingDetailsLink',
                            html: '' // filled in in Controller
                        }

                    ]
                }*/
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

