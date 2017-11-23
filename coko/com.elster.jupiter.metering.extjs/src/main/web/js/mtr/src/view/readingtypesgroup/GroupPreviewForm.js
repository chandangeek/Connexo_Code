/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.GroupPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.readingTypesGroup-preview-form',
    requires: [
    ],

    initComponent: function () {
        var me = this;
        me.items = [
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
                                fieldLabel: '&nbsp;',
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        name: 'commodity',
                                        itemId: 'mtr-readingTypesGroupPreview-commodity',
                                        fieldLabel: Uni.I18n.translate('readingTypes.attribute.commodity', 'MTR', 'Commodity')
                                    },
                                    {
                                        name: 'measurementKind',
                                        itemId: 'mtr-readingTypesGroupPreview-kind',
                                        fieldLabel: Uni.I18n.translate('readingTypes.attribute.kind', 'MTR', 'Kind')
                                    },
                                    {
                                        name: 'flowDirection',
                                        itemId: 'mtr-readingTypesGroupPreview-flowDirection',
                                        fieldLabel: Uni.I18n.translate('readingTypes.attribute.directionOfFlow', 'MTR', 'Direction of flow')
                                    },
                                    {
                                        name: 'unit',
                                        itemId: 'mtr-readingTypesGroupPreview-unit',
                                        fieldLabel: Uni.I18n.translate('readingTypes.attribute.unit', 'MTR', 'Unit')
                                    },
                                    {
                                        name: 'macroPeriod',
                                        itemId: 'mtr-readingTypesGroupPreview-macroPeriod',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.period', 'MTR', 'Period')
                                    },
                                    {
                                        name: 'timePeriod',
                                        itemId: 'mtr-readingTypesGroupPreview-timePeriod',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.timeperiod', 'MTR', 'Time-period of interest')
                                    },
                                    {
                                        name: 'aggregate',
                                        itemId: 'mtr-readingTypesGroupPreview-aggregate',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.aggregate', 'MTR', 'Aggregate')
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('readingTypesManagement.additionalParameters', 'MTR', 'Additional parameters'),
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        name: 'multiplier',
                                        itemId: 'mtr-readingTypesGroupPreview-multiplier',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.multiplier', 'MTR', 'Multiplier')
                                    },
                                    {
                                        name: 'phases',
                                        itemId: 'mtr-readingTypesGroupPreview-phase',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.phase', 'MTR', 'Phase')
                                    },
                                    {
                                        name: 'tou',
                                        itemId: 'mtr-readingTypesGroupPreview-tou',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.timeOfUse', 'MTR', 'Time of use')
                                    },
                                    {
                                        name: 'cpp',
                                        itemId: 'mtr-readingTypesGroupPreview-criticalPeakPeriod',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.criticalPeakPeriod', 'MTR', 'Critical peak period')
                                    },
                                    {
                                        name: 'consumptionTier',
                                        itemId: 'mtr-readingTypesGroupPreview-consumptionTier',
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.consumptionTier', 'MTR', 'Consumption tier')
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

