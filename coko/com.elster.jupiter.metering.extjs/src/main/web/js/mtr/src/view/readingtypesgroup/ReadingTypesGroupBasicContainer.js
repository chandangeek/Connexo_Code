/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.ReadingTypesGroupBasicContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.reading-types-group-basic-container',
    requires: [
        'Mtr.util.CimCombobox'
    ],
    defaults: {
        xtype: 'cimcombobox',
        labelWidth: 250,
        width: 506,
        displayField: 'displayName',
        valueField: 'code',
        editable: false,
        multiSelect: false,
        disabled: true,
        emptyText: Uni.I18n.translate('general.notApplicable', 'MTR', 'Not applicable'),
        cimField: 'code'
        // listeners: {
        //     change: function () {
        //         me.fireEvent('change', me)
        //     }
        // }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.commodity', 'MTR', '#6 Commodity'),
                store: 'Mtr.store.readingtypesgroup.attributes.Commodity',
                cimIndex: 6,
                name: 'commodity',
                disabled: false,
                listeners: {

                }
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', '#7 Measurement kind'),
                store: 'Mtr.store.readingtypesgroup.attributes.Kind',
                cimIndex: 7,
                name: 'measurementKind'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', '#5 Flow direction'),
                store: 'Mtr.store.readingtypesgroup.attributes.DirectionOfFlow',
                cimIndex: 5,
                disabled: true,
                name: 'flowDirection'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', '#17 Unit'),
                store: 'Mtr.store.readingtypesgroup.attributes.UnitOfMeasures',
                cimIndex: 17,
                name: 'unit'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', '#1 Macro period'),
                store: 'Mtr.store.readingtypesgroup.attributes.MacroPeriod',
                cimIndex: 1,
                disabled: false,
                name: 'macroPeriod',
                cimField: 'code'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', '#4 Accumulation'),
                store: 'Mtr.store.readingtypesgroup.attributes.Accumulation',
                emptyText: Uni.I18n.translate('readingTypesManagement.attribute.accumulation.emptyText', 'MTR', 'Select an accumulation...'),
                cimIndex: 4,
                hidden: true,
                disabled: false,
                name: 'accumulation'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', '#3 Measuring period'),
                store: 'Mtr.store.readingtypesgroup.attributes.MeasuringPeriod',
                emptyText: Uni.I18n.translate('readingTypesManagement.attribute.Accumulation.emptyText', 'MTR', 'Select a time period...'),
                cimIndex: 3,
                hidden: true,
                disabled: false,
                name: 'measuringPeriod'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', '#2 Aggregate'),
                store: 'Mtr.store.readingtypesgroup.attributes.Aggregate',
                cimIndex: 2,
                disabled: false,
                name: 'aggregate'
            },
            {
                xtype: 'label',
                fieldLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.additional.parameters', 'MTR', 'Additional parameters'),
                privileges: Mtr.privileges.ReadingTypes.admin,
                text: 'Additonal parameters',
                itemId: 'additional-Param',
                name: 'additionalParam',
                margin: '0 0 0 128',
                required: true,
                allowBlank: false,
                width: 685
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'reading-type-add-additional-parameters-fields-container',
                disabled: false,
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
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.multiplier', 'MTR', '#16 Multiplier'),
                        store: 'Mtr.store.readingtypesgroup.attributes.Multiplier',
                        cimIndex: 16,
                        hidden: true,
                        name: 'metricMultiplier'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', '#15 Phases'),
                        store: 'Mtr.store.readingtypesgroup.attributes.Phase',
                        cimIndex: 15,
                        hidden: true,
                        name: 'phases'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.timeOfUse', 'MTR', '#12 Time of use'),
                        store: 'Mtr.store.readingtypesgroup.attributes.TimeOfUse',
                        showCimCodes: false,
                        cimIndex: 12,
                        hidden: true,
                        name: 'tou'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.criticalPeakPeriod', 'MTR', '#13 Critical peak period'),
                        store: 'Mtr.store.readingtypesgroup.attributes.CriticalPeakPeriod',
                        showCimCodes: false,
                        cimIndex: 13,
                        hidden: true,
                        name: 'cpp'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.consumptionTier', 'MTR', '#14 Consumption tier'),
                        store: 'Mtr.store.readingtypesgroup.attributes.ConsumptionTier',
                        showCimCodes: false,
                        cimIndex: 14,
                        hidden: true,
                        name: 'consumptionTier'
                    }
                ]
            }
        ];
        me.callParent(arguments)
    }
});
