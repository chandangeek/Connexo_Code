/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.ReadingTypesGroupBasicContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.reading-types-group-basic-container',
    requires: [
        'Mtr.readingtypes.util.CimCombobox'
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
        cimField: 'code',
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
                store: 'Mtr.readingtypes.attributes.store.Commodity',
                cimIndex: 6,
                name: 'commodity',
                disabled: false,
                listeners: {
                    afterrender: function (combo) {
                        me.afterRenderCommodity(combo);
                    },
                    change: function (combo, newValue, oldValue) {
                        me.commodityChanged(newValue);
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', '#7 Measurement kind'),
                store: 'Mtr.readingtypes.attributes.store.Kind',
                cimIndex: 7,
                name: 'measurementKind'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', '#5 Flow direction'),
                store: 'Mtr.readingtypes.attributes.store.DirectionOfFlow',
                cimIndex: 5,
                name: 'flowDirection'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', '#17 Unit'),
                store: 'Mtr.readingtypes.attributes.store.UnitOfMeasures',
                cimIndex: 17,
                name: 'unit'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', '#1 Macro period'),
                store: 'Mtr.readingtypes.attributes.store.Interval',
                cimIndex: 1,
                name: 'macroPeriod'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', '#4 Accumulation'),
                store: 'Mtr.readingtypes.attributes.store.Accumulation',
                cimIndex: 4,
                name: 'accumulation'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', '#3 Measuring period'),
                store: 'Mtr.readingtypes.attributes.store.MeasuringPeriod',
                cimIndex: 3,
                name: 'measuringPeriod'
            },
            {
                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', '#2 Aggregate'),
                store: 'Mtr.readingtypes.attributes.store.DataQualifier',
                cimIndex: 2,
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
                        store: 'Mtr.readingtypes.attributes.store.Multiplier',
                        cimIndex: 16,
                        name: 'metricMultiplier'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', '#15 Phases'),
                        store: 'Mtr.readingtypes.attributes.store.Phase',
                        cimIndex: 15,
                        name: 'phases'
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
                    }
                ]
            }


        ];
        me.callParent(arguments)
    },
    afterRenderCommodity: function (combo) {
        var cm = [1, 2, 7, 9, 41];
        combo.getStore().addFilter([
            function (rec) {
                return cm.indexOf(rec.get('code')) >= 0;
            }
        ]);
    },
    commodityChanged: function (newValue) {
        var me = this;
        var measurementKind = me.down('[name=measurementKind]');

        //measurementKind.getValue() == 0;
        var mk = [];
        switch (newValue) {
            case 1:
            case 2:
                mk.push(4, 5, 8, 12, 37, 38, 158);
                break;
            case 7:
                mk.push(58, 12, 46, 155);
                break;
            case 9:
                mk.push(58, 46, 155);
                break;
            case 41:
                mk.push(94, 115, 46, 92);
                break;
        }
        measurementKind.getStore().clearFilter();
        measurementKind.getStore().addFilter([
            function (rec) {
                return mk.indexOf(rec.get('code')) >= 0;
            }
        ]);
        // measurementKind.getStore().filterBy(function(rec) {
        //
        // });

        measurementKind.setDisabled(false);

    }
});
