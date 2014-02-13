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
                    html: '<H4>' + I18n.translate('registerMapping.noRegisterMappingSelected', 'MDC', 'No register type selected') + '</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>' + I18n.translate('registerMapping.selectRegisterMapping', 'MDC', 'Select a register type to see its details') + '</H5>'
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
                    html: '<h4>' + I18n.translate('registerMapping.previewTitle', 'MDC', 'Selected register preview') + '</h4>',
                    itemId: 'registerMappingPreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: I18n.translate('general.actions', 'MDC', 'Actions'),
                    menu: {
                        items: [
                            {
                                text: I18n.translate('general.remove', 'MDC', 'Remove'),
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
                                    fieldLabel: I18n.translate('registerMapping.name', 'MDC', 'Name'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'mrid',
                                    fieldLabel: I18n.translate('registerMapping.readingType', 'MDC', 'Reading type'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'obisCode',
                                    fieldLabel: I18n.translate('registerMapping.obisCode', 'MDC', 'Obis Code'),
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
                                    name: 'measurementKind',
                                    fieldLabel: I18n.translate('registerMapping.type', 'MDC', 'Type'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'dataCollectionGroup',
                                    fieldLabel: I18n.translate('registerMapping.dataCollectionGroup', 'MDC', 'Data collection group'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                }

                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: I18n.translate('readingType.readingTypeDetails', 'MDC', 'Reading type details'),
                    layout: {
                        type: 'column'
                        //align: 'stretch'
                    },
                    collapsible: true,
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
                                    name: 'timePeriodOfInterest',
                                    fieldLabel: I18n.translate('readingType.timePeriodOfInterest', 'MDC', 'time-period of interest'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'dataQualifier',
                                    fieldLabel: I18n.translate('readingType.dataQualifier', 'MDC', 'Data qualifier'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'timeAttributeEnumerations',
                                    fieldLabel: I18n.translate('readingType.timeAttributeEnumerations', 'MDC', 'Time attribute enumerations'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'accumulationBehaviour',
                                    fieldLabel: I18n.translate('readingType.accumulationBehaviour', 'MDC', 'Accumulation behavior'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'directionOfFlow',
                                    fieldLabel: I18n.translate('readingType.directionOfFlow', 'MDC', 'Direction of flow'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'commodity',
                                    fieldLabel: I18n.translate('readingType.commodity', 'MDC', 'Commodity'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'measurementKind',
                                    fieldLabel: I18n.translate('readingType.measurementKind', 'MDC', 'Kind'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'interharmonics',
                                    fieldLabel: I18n.translate('readingType.interharmonics', 'MDC', '(Compound) Interharmonics'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'argumentReference',
                                    fieldLabel: I18n.translate('readingType.argumentReference', 'MDC', '(Compound) Numerator and Denominator Argument Reference'),
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
                                    name: 'timeOfUse',
                                    fieldLabel: I18n.translate('readingType.timeOfUse', 'MDC', 'Time of use'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'criticalPeakPeriod',
                                    fieldLabel: I18n.translate('readingType.criticalPeakPeriod', 'MDC', 'Critical peak period'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'consumptionTier',
                                    fieldLabel: I18n.translate('readingType.comsumptionTier', 'MDC', 'Consumption tier'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'phase',
                                    fieldLabel: I18n.translate('readingType.phase', 'MDC', 'Phase'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'powerOfTenMultiplier',
                                    fieldLabel: I18n.translate('readingType.powerOfTenMultiplier', 'MDC', 'Power of ten multiplier'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'unitOfMeasure',
                                    fieldLabel: I18n.translate('readingType.unitOfMeasure', 'MDC', 'Unit of measure'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'currency',
                                    fieldLabel: I18n.translate('readingType.currency', 'MDC', 'Currency'),
                                    labelAlign: 'right',
                                    labelWidth: 150
                                }
                            ]
                        }
                    ]
                }
                /*,
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

