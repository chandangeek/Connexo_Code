/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-preview-form',
    itemId: 'reading-types-preview-form',
    requires: [
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                name: 'mRID',
                labelWidth: 200,
                itemId: 'cim-code',
                fieldLabel: Uni.I18n.translate('readingtypesmanagment.CimCode', 'MTR', 'CIM code')
            },
            {
                layout: 'column',
                defaults: {
                    xtype: 'form',
                    columnWidth: 0.5
                },
                items: [
                    {
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('readingtypesmanagment.cimCodeDetails', 'MTR', 'CIM code details'),
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 200
                                },
                                items: [
                                    {
                                        name: 'macroPeriod',
                                        itemId: 'macro-period',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.timeperiod', 'MTR', 'Time-period of interest')
                                    },
                                    {
                                        name: 'aggregate',
                                        itemId: 'aggregate',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.dataqualifer', 'MTR', 'Data qualifier')
                                    },
                                    {
                                        name: 'measuringPeriod',
                                        itemId: 'measuring-period',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.time', 'MTR', 'Time')
                                    },
                                    {
                                        name: 'accumulation',
                                        itemId: 'accumulation',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.accumulation', 'MTR', 'Accumulation')
                                    },
                                    {
                                        name: 'flowDirection',
                                        itemId: 'flow-direction',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.flowDirection', 'MTR', 'Direction of flow')
                                    },
                                    {
                                        name: 'commodity',
                                        itemId: 'commodity',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.commodity', 'MTR', 'Commodity')
                                    },
                                    {
                                        name: 'measurementKind',
                                        itemId: 'measurement-kind',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.kind', 'MTR', 'Kind')
                                    },
                                    {
                                        name: 'interHarmonicNumerator',
                                        itemId: 'inter-harmonic-numerator',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.interharmonicnumeration', 'MTR', 'Interharmonic numerator')
                                    },
                                    {
                                        name: 'interHarmonicDenominator',
                                        itemId: 'inter-harmonic-denominator',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.interharmonicdenumeration', 'MTR', 'Interharmonic denominator')
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                labelAlign: 'top',
                                layout: 'vbox',
                                fieldLabel: '&nbsp;',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 200
                                },
                                items: [
                                    {
                                        name: 'argumentNumerator',
                                        itemId: 'argument-numerator',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.argumentNumerical', 'MTR', 'Argument numerator')
                                    },
                                    {
                                        name: 'argumentDenominator',
                                        itemId: 'argument-denominator',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.argumentDenominator', 'MTR', 'Argument denominator')
                                    },
                                    {
                                        name: 'tou',
                                        itemId: 'time-of-use',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.timeOfUse', 'MTR', 'Time of use')
                                    },
                                    {
                                        name: 'cpp',
                                        itemId: 'critical-peak-period',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.criticalPeakPeriod', 'MTR', 'Critical peak period')
                                    },
                                    {
                                        name: 'consumptionTier',
                                        itemId: 'consumption-tier',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.consumptionTier', 'MTR', 'Consumption tier')
                                    },
                                    {
                                        name: 'phases',
                                        itemId: 'phases',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.phase', 'MTR', 'Phase')
                                    },
                                    {
                                        name: 'metricMultiplier',
                                        itemId: 'metric-multiplier',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.multiplaier', 'MTR', 'Multiplier')
                                    },
                                    {
                                        name: 'unit',
                                        itemId: 'unit',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.unitOfMeasure', 'MTR', 'Unit of measure')
                                    },
                                    {
                                        name: 'currency',
                                        itemId: 'currency',
                                        fieldLabel: Uni.I18n.translate('readingtypesmanagment.currency', 'MTR', 'Currency')
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
