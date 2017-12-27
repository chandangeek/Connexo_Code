/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypes.AddReadingTypesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-reading-types-form',
    requires: [
        'Mtr.util.CimCombobox'
    ],
    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                itemId: 'form-errors',
                xtype: 'uni-form-error-message',
                name: 'form-errors',
                hidden: true,
                maxWidth: 512
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.alias', 'MTR', 'Reading type set'),
                itemId: 'alias-name',
                name: 'aliasName',
                required: true,
                allowBlank: false,
                width: 506
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.specifyBy', 'MTR', 'Specify by'),
                layout: 'hbox',
                vertical: true,
                items: [
                    {
                        xtype: 'radiogroup',
                        boxLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.cimCode', 'MTR', 'CIM code'),
                        layout: 'vbox',
                        itemId: 'specify-by-radiogroup',
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.cimCode', 'MTR', 'CIM code'),
                                name: 'specifyBy',
                                inputValue: 'cim',
                                checked: true
                            },
                            {
                                boxLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.form', 'MTR', 'Form'),
                                name: 'specifyBy',
                                inputValue: 'form'
                            }
                        ],
                        listeners: {
                            change: function (group, newValue) {
                                var cimField = me.down('textfield[name=mRID]'),
                                    form = me.down('#reading-type-add-fields-container');
                                me.fireEvent('switchmode', newValue.specifyBy, me);
                                switch (newValue.specifyBy) {
                                    case 'cim':
                                        form.disable();
                                        cimField.enable();
                                        break;
                                    case 'form':
                                        form.enable();
                                        cimField.disable();
                                        break;
                                }
                            }
                        }
                    },
                    {
                        xtype: 'textfield',
                        margin: '0 0 0 128',
                        emptyText: 'x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x',
                        itemId: 'cim-code-field',
                        required: true,
                        allowBlank: false,
                        name: 'mRID',
                        width: 420,
                        afterSubTpl: '<div class="x-form-display-field"><i>' + Uni.I18n.translate('readingTypesManagement.addReadingTypes.cimCodeValuesInstruction', 'MTR', "Provide the values for the 18 attributes of the CIM code, separated by a ' . '") + '</i></div>'
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'reading-type-add-fields-container',
                disabled: true,
                defaults: {
                    xtype: 'cimcombobox',
                    labelWidth: 250,
                    width: 506,
                    displayField: 'displayName',
                    valueField: 'code',
                    editable: false,
                    multiSelect: true,
                    emptyText: Uni.I18n.translate('general.notApplicable', 'MTR', 'Not applicable'),
                    cimField: 'code',
                    listeners: {
                        change: function () {
                            me.fireEvent('change', me)
                        }
                    }
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', 'Period #1'),
                        store: 'Mtr.store.readingtypes.attributes.Interval',
                        cimIndex: 1,
                        name: 'macroPeriod'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', 'Aggregate #2'),
                        store: 'Mtr.store.readingtypes.attributes.DataQualifier',
                        cimIndex: 2,
                        name: 'aggregate'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', 'Time #3'),
                        store: 'Mtr.store.readingtypes.attributes.MeasuringPeriod',
                        cimIndex: 3,
                        name: 'measuringPeriod'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', 'Accumulation #4'),
                        store: 'Mtr.store.readingtypes.attributes.Accumulation',
                        cimIndex: 4,
                        name: 'accumulation'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', 'Direction of flow #5'),
                        store: 'Mtr.store.readingtypes.attributes.DirectionOfFlow',
                        cimIndex: 5,
                        name: 'flowDirection'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.commodity', 'MTR', 'Commodity #6'),
                        store: 'Mtr.store.readingtypes.attributes.Commodity',
                        cimIndex: 6,
                        name: 'commodity'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', 'Kind #7'),
                        store: 'Mtr.store.readingtypes.attributes.Kind',
                        cimIndex: 7,
                        name: 'measurementKind'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.interharmonicNumerator', 'MTR', 'Interharmonic numerator #8'),
                        store: 'Mtr.store.readingtypes.attributes.InterharmonicNumerator',
                        cimIndex: 8,
                        name: 'interHarmonicNumerator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.interharmonicDenominator', 'MTR', 'Interharmonic denominator #9'),
                        store: 'Mtr.store.readingtypes.attributes.InterharmonicDenominator',
                        cimIndex: 9,
                        name: 'interHarmonicDenominator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.argumentNumerator', 'MTR', 'Argument numerator #10'),
                        store: 'Mtr.store.readingtypes.attributes.ArgumentNumerator',
                        cimIndex: 10,
                        name: 'argumentNumerator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.argumentDenominator', 'MTR', 'Argument denominator #11'),
                        store: 'Mtr.store.readingtypes.attributes.ArgumentDenominator',
                        cimIndex: 11,
                        name: 'argumentDenominator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.timeOfUse', 'MTR', 'Time of use #12'),
                        store: 'Mtr.store.readingtypes.attributes.TimeOfUse',
                        showCimCodes: false,
                        cimIndex: 12,
                        name: 'tou'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.criticalPeakPeriod', 'MTR', 'Critical peak period #13'),
                        store: 'Mtr.store.readingtypes.attributes.CriticalPeakPeriod',
                        showCimCodes: false,
                        cimIndex: 13,
                        name: 'cpp'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.consumptionTier', 'MTR', 'Consumption tier #14'),
                        store: 'Mtr.store.readingtypes.attributes.ConsumptionTier',
                        showCimCodes: false,
                        cimIndex: 14,
                        name: 'consumptionTier'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', 'Phases #15'),
                        store: 'Mtr.store.readingtypes.attributes.Phase',
                        cimIndex: 15,
                        name: 'phases'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.multiplier', 'MTR', 'Scale #16'),
                        store: 'Mtr.store.readingtypes.attributes.Multiplier',
                        cimIndex: 16,
                        name: 'metricMultiplier'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', 'Unit #17'),
                        store: 'Mtr.store.readingtypes.attributes.UnitOfMeasures',
                        cimIndex: 17,
                        name: 'unit'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.currency', 'MTR', 'Currency #18'),
                        store: 'Mtr.store.readingtypes.attributes.Currency',
                        cimIndex: 18,
                        name: 'currency'
                    }
                ]
            }
        ];
        me.callParent(arguments)
    }
});
