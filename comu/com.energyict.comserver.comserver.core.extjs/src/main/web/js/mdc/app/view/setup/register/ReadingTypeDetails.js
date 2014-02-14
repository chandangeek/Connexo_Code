Ext.define('Mdc.view.setup.register.ReadingTypeDetails', {
    extend: 'Ext.window.Window',
    alias: 'widget.readingTypeDetails',
    autoScroll: true,
    itemId: 'readingTypeDetails',
    requires: [
        'Uni.view.breadcrumb.Trail'
    ],
    closable: true,
    width: 700,
    height: 500,
    modal: true,
    constrain: true,
    autoShow: true,
    modal:true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    closeAction : 'destroy',
    floating:true,
    cls: 'content-wrapper',
//    border: 0,
//    region: 'center',

    items: [

        {
            xtype: 'form',

            border: false,
            itemId: 'readingTypeDetailsForm',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + I18n.translate('readingType.readingTypeDetails', 'MDC', 'Reading type details') + '</h4>',
                    itemId: 'registerMappingPreviewTitle'
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
                                    name: 'timePeriodOfInterest',
                                    fieldLabel: I18n.translate('readingType.timePeriodOfInterest', 'MDC', 'Time-period of interest'),
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
            ]
        }

    ]

});
