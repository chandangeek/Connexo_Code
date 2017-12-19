/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.AddReadingTypesGroupForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-reading-types-group-form',
    requires: [
        'Mtr.util.CimCombobox',
        'Mtr.model.readingtypesgroup.AddExtendedReadingTypeGroup',
        'Mtr.model.readingtypesgroup.AddBasicReadingTypeGroup'
    ],
    defaults: {
        labelWidth: 250
    },

    getBasicRecord: function () {
        var record = this.updateRecord().getRecord();
        var basicRecord = Ext.create('Mtr.model.readingtypesgroup.AddBasicReadingTypeGroup');

        var fields = basicRecord.self.getFields()
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            basicRecord.set(field.name, record.get(field.name));
        }
        return basicRecord;
    },

    getExtendedRecord: function () {
        var record = this.updateRecord().getRecord();
        var extendedRecord = Ext.create('Mtr.model.readingtypesgroup.AddExtendedReadingTypeGroup');
        var fields = extendedRecord.self.getFields()
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            extendedRecord.set(field.name, record.get(field.name));
        }
        extendedRecord.set('specifyBy', record.get('specifyBy'));
        return extendedRecord;
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
                fieldLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.alias', 'MTR', 'Name'),
                itemId: 'alias-name',
                name: 'aliasName',
                required: true,
                allowBlank: false,
                width: 685
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
                                inputValue: 'cim'
                            },
                            {
                                boxLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.form', 'MTR', 'Form'),
                                name: 'specifyBy',
                                inputValue: 'form',
                                checked: true
                            }
                        ],
                        listeners: {
                            change: function (group, newValue) {
                                var cimField = me.down('textfield[name=mRID]'),
                                    tabFormBasic = me.down('#reading-types-groups-add-basic-tab'),
                                    tabFormExtended = me.down('#reading-types-groups-add-extended-tab');
                                cim_form = newValue.specifyBy;
                                switch (newValue.specifyBy) {
                                    case 'cim':
                                        cimField.enable();
                                        tabFormBasic.disable();
                                        tabFormExtended.disable();
                                        break;
                                    case 'form':
                                        tabFormBasic.enable();
                                        tabFormExtended.enable();
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
                        disabled: true,
                        name: 'mRID',
                        width: 420,
                        afterSubTpl: '<div class="x-form-display-field"><i>' + Uni.I18n.translate('readingTypesManagement.addReadingTypes.cimCodeValuesInstruction', 'MTR', "Provide the values for the 18 attributes of the CIM code, separated by a ' . '") + '</i></div>'
                    }
                ]
            },
            {
                xtype: 'tabpanel',
                margin: '20 0 0 0',
                itemId: 'reading-types-add-group-tab-panel',
                activeTab: me.activeTab,
                width: '100%',
                items: [
                    {
                        title: Uni.I18n.translate('general.basic', 'MTR', 'Basic'),
                        padding: '8 16 16 0',
                        itemId: 'reading-types-groups-add-basic-tab',
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'reading-types-group-basic-container',
                                disabled: false,
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
                                    listeners: {
                                        change: function (a, b) {
                                            me.fireEvent('change', me);
                                        }
                                    }
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.commodity', 'MTR', '#6 Commodity'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Commodity',
                                        cimIndex: 6,
                                        name: 'basicCommodity',
                                        required: true,
                                        disabled: false
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', '#7 Measurement kind'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Kind',
                                        cimIndex: 7,
                                        name: 'basicMeasurementKind',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', '#5 Flow direction'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.DirectionOfFlow',
                                        cimIndex: 5,
                                        disabled: true,
                                        name: 'basicFlowDirection',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', '#17 Unit'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.UnitOfMeasures',
                                        cimIndex: 17,
                                        disabled: true,
                                        name: 'basicUnit',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', '#1 Macro period'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.MacroPeriod',
                                        cimIndex: 1,
                                        disabled: false,
                                        name: 'basicMacroPeriod',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', '#4 Accumulation'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Accumulation',
                                        emptyText: Uni.I18n.translate('readingTypesManagement.attribute.accumulation.emptyText', 'MTR', 'Select an accumulation...'),
                                        cimIndex: 4,
                                        hidden: true,
                                        disabled: false,
                                        name: 'basicAccumulation'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', '#3 Measuring period'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.MeasuringPeriod',
                                        emptyText: Uni.I18n.translate('readingTypesManagement.attribute.Accumulation.emptyText', 'MTR', 'Select a time period...'),
                                        cimIndex: 3,
                                        hidden: true,
                                        disabled: false,
                                        name: 'basicMeasuringPeriod'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', '#2 Aggregate'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Aggregate',
                                        cimIndex: 2,
                                        disabled: false,
                                        required: true,
                                        name: 'basicAggregate'
                                    },
                                    {

                                        xtype: 'tabpanel',
                                        title: Uni.I18n.translate('readingTypesManagement.addReadingTypes.additional.parameters', 'MTR', 'Additional parameters'),
                                        ui: 'medium',
                                        disabled: false,
                                        margin: '30 0 0 50'
                                    },

                                    {
                                        margin: '0 0 0 260',
                                        xtype: 'uni-form-empty-message',
                                        itemId: 'no-additional-parameters',
                                        text: Uni.I18n.translate('readingTypesManagement.addReadingTypes.additional.noCommodity', 'MTR', 'Select a Commodity to specify additional parameters')
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
                                            multiSelect: false,
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
                                                name: 'basicMetricMultiplier'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', '#15 Phases'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.Phase',
                                                cimIndex: 15,
                                                hidden: true,
                                                name: 'basicPhases'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.timeOfUse', 'MTR', '#12 Time of use'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.TimeOfUse',
                                                showCimCodes: false,
                                                cimIndex: 12,
                                                hidden: true,
                                                name: 'basicTou'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.criticalPeakPeriod', 'MTR', '#13 Critical peak period'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.CriticalPeakPeriod',
                                                showCimCodes: false,
                                                cimIndex: 13,
                                                hidden: true,
                                                name: 'basicCpp'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.consumptionTier', 'MTR', '#14 Consumption tier'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.ConsumptionTier',
                                                showCimCodes: false,
                                                cimIndex: 14,
                                                hidden: true,
                                                name: 'basicConsumptionTier'
                                            }
                                        ]  // basic aditional parameters end
                                    }
                                ]  // basic items end
                            },
                            {
                                xtype: 'displayfield',
                                labelWidth: 250,
                                fieldLabel: '&nbsp',
                                itemId: 'add-reading-types-basic-description-of-attributes-info',
                                value: Uni.I18n.translate('readingtypesmanagement.addreadingtypes.cimFormDescriptionOfAttributesInfo', 'MTR', 'Description of attributes can be found in CIM documentation'),
                                renderer: function (value) {
                                    return '<i>' + value + '</i>'
                                }
                            },
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.extended', 'MTR', 'Extended'),
                        padding: '8 16 16 0',
                        itemId: 'reading-types-groups-add-extended-tab',
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'reading-types-group-extended-container',
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
                                            me.fireEvent('change', me);
                                        }
                                    }
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', '#1 Macro period'),
                                        store: 'Mtr.store.readingtypes.attributes.Interval',
                                        cimIndex: 1,
                                        name: 'macroPeriod'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', '#2 Aggregate'),
                                        store: 'Mtr.store.readingtypes.attributes.DataQualifier',
                                        cimIndex: 2,
                                        name: 'aggregate'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', '#3 Measuring period'),
                                        store: 'Mtr.store.readingtypes.attributes.MeasuringPeriod',
                                        cimIndex: 3,
                                        name: 'measuringPeriod'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', '#4 Accumulation'),
                                        store: 'Mtr.store.readingtypes.attributes.Accumulation',
                                        cimIndex: 4,
                                        name: 'accumulation'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', '#5 Flow direction'),
                                        store: 'Mtr.store.readingtypes.attributes.DirectionOfFlow',
                                        cimIndex: 5,
                                        name: 'flowDirection'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.commodity', 'MTR', '#6 Commodity'),
                                        store: 'Mtr.store.readingtypes.attributes.Commodity',
                                        cimIndex: 6,
                                        name: 'commodity'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', '#7 Measurement kind'),
                                        store: 'Mtr.store.readingtypes.attributes.Kind',
                                        cimIndex: 7,
                                        name: 'measurementKind'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.interharmonicNumerator', 'MTR', '#8 Interharmonic numerator'),
                                        store: 'Mtr.store.readingtypes.attributes.InterharmonicNumerator',
                                        cimIndex: 8,
                                        name: 'interHarmonicNumerator',
                                        showCimCodes: false
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.interharmonicDenominator', 'MTR', '#9 Interharmonic denominator'),
                                        store: 'Mtr.store.readingtypes.attributes.InterharmonicDenominator',
                                        cimIndex: 9,
                                        name: 'interHarmonicDenominator',
                                        showCimCodes: false
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.argumentNumerator', 'MTR', '#10 Argument numerator'),
                                        store: 'Mtr.store.readingtypes.attributes.ArgumentNumerator',
                                        cimIndex: 10,
                                        name: 'argumentNumerator',
                                        showCimCodes: false
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.argumentDenominator', 'MTR', '#11 Argument denominator'),
                                        store: 'Mtr.store.readingtypes.attributes.ArgumentDenominator',
                                        cimIndex: 11,
                                        name: 'argumentDenominator',
                                        showCimCodes: false
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.timeOfUse', 'MTR', '#12 Time of use'),
                                        store: 'Mtr.store.readingtypes.attributes.TimeOfUse',
                                        showCimCodes: false,
                                        cimIndex: 12,
                                        name: 'tou'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.criticalPeakPeriod', 'MTR', '#13 Critical peak period'),
                                        store: 'Mtr.store.readingtypes.attributes.CriticalPeakPeriod',
                                        showCimCodes: false,
                                        cimIndex: 13,
                                        name: 'cpp'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.consumptionTier', 'MTR', '#14 Consumption tier'),
                                        store: 'Mtr.store.readingtypes.attributes.ConsumptionTier',
                                        showCimCodes: false,
                                        cimIndex: 14,
                                        name: 'consumptionTier'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', '#15 Phases'),
                                        store: 'Mtr.store.readingtypes.attributes.Phase',
                                        cimIndex: 15,
                                        name: 'phases'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.multiplier', 'MTR', '#16 Multiplier'),
                                        store: 'Mtr.store.readingtypes.attributes.Multiplier',
                                        cimIndex: 16,
                                        name: 'metricMultiplier'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', '#17 Unit'),
                                        store: 'Mtr.store.readingtypes.attributes.UnitOfMeasures',
                                        cimIndex: 17,
                                        name: 'unit'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.currency', 'MTR', '#18 Currency'),
                                        store: 'Mtr.store.readingtypes.attributes.Currency',
                                        cimIndex: 18,
                                        name: 'currency'
                                    }
                                ]
                            },
                            {
                                xtype: 'displayfield',
                                labelWidth: 250,
                                fieldLabel: '&nbsp',
                                itemId: 'add-reading-types-extended-description-of-attributes-info',
                                value: Uni.I18n.translate('readingtypesmanagement.addreadingtypes.cimFormDescriptionOfAttributesInfo', 'MTR', 'Description of attributes can be found in CIM documentation'),
                                renderer: function (value) {
                                    return '<i>' + value + '</i>'
                                }
                            },
                        ]
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                labelWidth: 250,
                fieldLabel: '&nbsp',
                items: [
                    {
                        text: Uni.I18n.translate('general.add', 'MTR', 'Add'),
                        xtype: 'button',
                        ui: 'action',
                        action: 'add',
                        itemId: 'add-reading-types-group-general-add-button'
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MTR', 'Cancel'),
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'add-reading-types-group-general-cancel-button',
                        href: ''
                    }
                ]
            }
        ];
        me.callParent(arguments)
    },
});
