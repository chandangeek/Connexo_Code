/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.AddReadingTypesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-reading-types-form',
    requires: [
        'Mtr.readingtypes.util.CimCombobox'
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
                fieldLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.alias', 'MTR', 'Alias'),
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
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', '#1 Macro period'),
                        store: 'Mtr.readingtypes.attributes.store.Interval',
                        cimIndex: 1,
                        name: 'macroPeriod'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', '#2 Aggregate'),
                        store: 'Mtr.readingtypes.attributes.store.DataQualifier',
                        cimIndex: 2,
                        name: 'aggregate'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', '#3 Measuring period'),
                        store: 'Mtr.readingtypes.attributes.store.MeasuringPeriod',
                        cimIndex: 3,
                        name: 'measuringPeriod'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', '#4 Accumulation'),
                        store: 'Mtr.readingtypes.attributes.store.Accumulation',
                        cimIndex: 4,
                        name: 'accumulation'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', '#5 Flow direction'),
                        store: 'Mtr.readingtypes.attributes.store.DirectionOfFlow',
                        cimIndex: 5,
                        name: 'flowDirection'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.commodity', 'MTR', '#6 Commodity'),
                        store: 'Mtr.readingtypes.attributes.store.Commodity',
                        cimIndex: 6,
                        name: 'commodity'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', '#7 Measurement kind'),
                        store: 'Mtr.readingtypes.attributes.store.Kind',
                        cimIndex: 7,
                        name: 'measurementKind'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.interharmonicNumerator', 'MTR', '#8 Interharmonic numerator'),
                        store: 'Mtr.readingtypes.attributes.store.InterharmonicNumerator',
                        cimIndex: 8,
                        name: 'interHarmonicNumerator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.interharmonicDenominator', 'MTR', '#9 Interharmonic denominator'),
                        store: 'Mtr.readingtypes.attributes.store.InterharmonicDenominator',
                        cimIndex: 9,
                        name: 'interHarmonicDenominator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.argumentNumerator', 'MTR', '#10 Argument numerator'),
                        store: 'Mtr.readingtypes.attributes.store.ArgumentNumerator',
                        cimIndex: 10,
                        name: 'argumentNumerator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.argumentDenominator', 'MTR', '#11 Argument denominator'),
                        store: 'Mtr.readingtypes.attributes.store.ArgumentDenominator',
                        cimIndex: 11,
                        name: 'argumentDenominator',
                        showCimCodes: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.timeOfUse', 'MTR', '#12 Time of use'),
                        store: 'Mtr.readingtypes.attributes.store.TimeOfUse',
                        showCimCodes: false,
                        cimIndex: 12,
                        name: 'tou'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.criticalPeakPeriod', 'MTR', '#13 Critical peak period'),
                        store: 'Mtr.readingtypes.attributes.store.CriticalPeakPeriod',
                        showCimCodes: false,
                        cimIndex: 13,
                        name: 'cpp'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.consumptionTier', 'MTR', '#14 Consumption tier'),
                        store: 'Mtr.readingtypes.attributes.store.ConsumptionTier',
                        showCimCodes: false,
                        cimIndex: 14,
                        name: 'consumptionTier'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', '#15 Phases'),
                        store: 'Mtr.readingtypes.attributes.store.Phase',
                        cimIndex: 15,
                        name: 'phases'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.multiplier', 'MTR', '#16 Multiplier'),
                        store: 'Mtr.readingtypes.attributes.store.Multiplier',
                        cimIndex: 16,
                        name: 'metricMultiplier'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', '#17 Unit'),
                        store: 'Mtr.readingtypes.attributes.store.UnitOfMeasures',
                        cimIndex: 17,
                        name: 'unit'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.currency', 'MTR', '#18 Currency'),
                        store: 'Mtr.readingtypes.attributes.store.Currency',
                        cimIndex: 18,
                        name: 'currency'
                    }
                ]
            }
        ];
        me.callParent(arguments)
    }
});
