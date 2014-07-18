Ext.define('Uni.view.window.ReadingTypeDetails', {
    extend: 'Ext.window.Window',
    xtype: 'readingTypeDetails',
    closable: true,
    width: 700,
    height: 500,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    title: Uni.I18n.translate('readingType.readingTypeDetails', 'UNI', 'Reading type details'),
    items: {
        xtype: 'form',
        border: false,
        itemId: 'readingTypeDetailsForm',
        layout: 'column',
        defaults: {
            columnWidth: 0.5,
            layout: 'form'
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'timePeriodOfInterest',
                        fieldLabel: Uni.I18n.translate('readingType.timePeriodOfInterest', 'UNI', 'Time-period of interest')
                    },
                    {
                        name: 'dataQualifier',
                        fieldLabel: Uni.I18n.translate('readingType.dataQualifier', 'UNI', 'Data qualifier')
                    },
                    {
                        name: 'timeAttributeEnumerations',
                        fieldLabel: Uni.I18n.translate('readingType.timeAttributeEnumerations', 'UNI', 'Time attribute enumerations')
                    },
                    {
                        name: 'accumulationBehaviour',
                        fieldLabel: Uni.I18n.translate('readingType.accumulationBehaviour', 'UNI', 'Accumulation behavior')
                    },
                    {
                        name: 'directionOfFlow',
                        fieldLabel: Uni.I18n.translate('readingType.directionOfFlow', 'UNI', 'Direction of flow')
                    },
                    {
                        name: 'commodity',
                        fieldLabel: Uni.I18n.translate('readingType.commodity', 'UNI', 'Commodity')
                    },
                    {
                        name: 'measurementKind',
                        fieldLabel: Uni.I18n.translate('readingType.measurementKind', 'UNI', 'Kind')
                    },
                    {
                        name: 'interharmonics',
                        fieldLabel: Uni.I18n.translate('readingType.interharmonics', 'UNI', '(Compound) Interharmonics')
                    },
                    {
                        name: 'argumentReference',
                        fieldLabel: Uni.I18n.translate('readingType.argumentReference', 'UNI', '(Compound) Numerator and Denominator Argument Reference')
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'timeOfUse',
                        fieldLabel: Uni.I18n.translate('readingType.timeOfUse', 'UNI', 'Time of use')
                    },
                    {
                        name: 'criticalPeakPeriod',
                        fieldLabel: Uni.I18n.translate('readingType.criticalPeakPeriod', 'UNI', 'Critical peak period')
                    },
                    {
                        name: 'consumptionTier',
                        fieldLabel: Uni.I18n.translate('readingType.comsumptionTier', 'UNI', 'Consumption tier')
                    },
                    {
                        name: 'phase',
                        fieldLabel: Uni.I18n.translate('readingType.phase', 'UNI', 'Phase')
                    },
                    {
                        name: 'powerOfTenMultiplier',
                        fieldLabel: Uni.I18n.translate('readingType.powerOfTenMultiplier', 'UNI', 'Power of ten multiplier')
                    },
                    {
                        name: 'unitOfMeasure',
                        fieldLabel: Uni.I18n.translate('readingType.unitOfMeasure', 'UNI', 'Unit of measure')
                    },
                    {
                        name: 'currency',
                        fieldLabel: Uni.I18n.translate('readingType.currency', 'UNI', 'Currency')
                    }
                ]
            }
        ]
    }
});
