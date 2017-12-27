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
    addBasicCount: 0,
    addExtendedCount: 0,

    setBasicAddCount: function (count) {
        this.addBasicCount = count;
        this.down('#add-reading-types-basic-count').setValue(Uni.I18n.translatePlural('readingTypesManagement.addreadingtypes',
            count, 'MTR', 'No reading types will be added',
            'You are going to add {0} reading type. 1000 is the limit', 'You are going to add {0} reading types. 1000 is the limit')
        );
    },

    updateBasicAddCount: function () {
        var me = this,
            count = 0;
        var data = me.getBasicRecord().getData();

        delete data.id;
        delete data.mRID;
        delete data.aliasName;
        delete data.specifyBy;

        if (data.basicCommodity == 2) //Electricity 1 & 2
        {
            count = 2;  //lori
        }
        else {
            count = 0;
        }
        for (key in data) {
            if (data[key] instanceof Array) {
                if (!Ext.isEmpty(data[key][0])) {
                    !count && (count = 1);
                    count = count * data[key].length;
                }
            }
        }
        //update count
        // for (key in data) {
        //     if (!Ext.isEmpty(data[key])) {
        //         !count && (count = 1);
        //         count = count * 1;
        //         if (data.basicCommodity == 2) //Elctricity 1 & 2
        //         {
        //             count = 2;  //lori
        //         }
        //     }
        // }
        me.setBasicAddCount(count);
    },
    setExtendedAddCount: function (count) {
        this.addExtendedCount = count;
        this.down('#add-reading-types-extended-count').setValue(Uni.I18n.translatePlural('readingtypesmanagment.addreadingtypesgroup.countMsg',
            count, 'MTR', 'No reading types will be added',
            'You are going to add {0} reading type. 1000 is the limit', 'You are going to add {0} reading types. 1000 is the limit')
        );
    },

    updateExtendedAddCount: function () {
        var me = this,
            count = 0;
        var data = me.getExtendedRecord().getData();

        delete data.id;
        delete data.mRID;
        delete data.aliasName;
        delete data.specifyBy;
        for (key in data) {
            if (data[key] instanceof Array) {
                if (!Ext.isEmpty(data[key][0])) {
                    !count && (count = 1);
                    count = count * data[key].length;
                }
            }
        }
        me.setExtendedAddCount(count);
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
                fieldLabel: Uni.I18n.translate('readingTypesManagement.addReadingTypes.alias', 'MTR', 'Reading type set'),
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
                                        me.setBasicAddCount(1);
                                        me.setExtendedAddCount(1);
                                        break;
                                    case 'form':
                                        tabFormBasic.enable();
                                        tabFormExtended.enable();
                                        cimField.disable();
                                        me.updateBasicAddCount();
                                        me.updateExtendedAddCount();
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
                                            me.updateBasicAddCount();
                                        }
                                    }
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.commodity', 'MTR', 'Commodity #6'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Commodity',
                                        cimIndex: 6,
                                        name: 'basicCommodity',
                                        required: true,
                                        disabled: false
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', 'Kind #7'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Kind',
                                        cimIndex: 7,
                                        name: 'basicMeasurementKind',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', 'Direction of flow  #5'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.DirectionOfFlow',
                                        cimIndex: 5,
                                        disabled: true,
                                        name: 'basicFlowDirection',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.unit', 'MTR', 'Unit #17'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.UnitOfMeasures',
                                        cimIndex: 17,
                                        disabled: true,
                                        name: 'basicUnit',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.macroPeriod', 'MTR', 'Period #1'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.MacroPeriod',
                                        cimIndex: 1,
                                        disabled: false,
                                        name: 'basicMacroPeriod',
                                        required: true
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.accumulation', 'MTR', 'Accumulation #4'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.Accumulation',
                                        emptyText: Uni.I18n.translate('readingTypesManagement.attribute.accumulation.emptyText', 'MTR', 'Select an accumulation...'),
                                        cimIndex: 4,
                                        hidden: true,
                                        disabled: false,
                                        name: 'basicAccumulation'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measuringPeriod', 'MTR', 'Time period #3'),
                                        store: 'Mtr.store.readingtypesgroup.attributes.MeasuringPeriod',
                                        emptyText: Uni.I18n.translate('readingTypesManagement.attribute.Accumulation.emptyText', 'MTR', 'Select a time period...'),
                                        cimIndex: 3,
                                        hidden: true,
                                        disabled: false,
                                        name: 'basicMeasuringPeriod'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.aggregate', 'MTR', 'Aggregate #2'),
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
                                            multiSelect: true,
                                            emptyText: Uni.I18n.translate('general.notApplicable', 'MTR', 'Not applicable'),
                                            cimField: 'code',
                                            listeners: {
                                                change: function () {
                                                    me.fireEvent('change', me)
                                                    me.updateBasicAddCount();
                                                }
                                            }
                                        },
                                        items: [
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.multiplier', 'MTR', 'Scale #16'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.Multiplier',
                                                cimIndex: 16,
                                                hidden: true,
                                                name: 'basicMetricMultiplier'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.phases', 'MTR', 'Phases #15'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.Phase',
                                                cimIndex: 15,
                                                hidden: true,
                                                name: 'basicPhases'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.timeOfUse', 'MTR', 'Time of use #12'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.TimeOfUse',
                                                showCimCodes: false,
                                                cimIndex: 12,
                                                hidden: true,
                                                name: 'basicTou'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.criticalPeakPeriod', 'MTR', 'Critical peak period #13'),
                                                store: 'Mtr.store.readingtypesgroup.attributes.CriticalPeakPeriod',
                                                showCimCodes: false,
                                                cimIndex: 13,
                                                hidden: true,
                                                name: 'basicCpp'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.consumptionTier', 'MTR', 'Consumption tier #14'),
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
                            {
                                xtype: 'displayfield',
                                labelWidth: 250,
                                fieldLabel: '&nbsp',
                                fieldCls: 'x-panel-body-form-error',
                                itemId: 'add-reading-types-basic-count',
                                value: Uni.I18n.translate('readingtypesmanagement.addreadingtypes.defaultCountMsg', 'MTR', 'You are going to add 1 reading type. 1000 is the limit')
                            }
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
                                            me.updateExtendedAddCount();
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
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.flowDirection', 'MTR', 'Flow direction #5'),
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
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.measurementKind', 'MTR', 'Measurement kind #7'),
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
                                        fieldLabel: Uni.I18n.translate('readingTypesManagement.attribute.multiplier', 'MTR', 'Multiplier #16'),
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
                            {
                                xtype: 'displayfield',
                                labelWidth: 250,
                                fieldLabel: '&nbsp',
                                fieldCls: 'x-panel-body-form-error',
                                itemId: 'add-reading-types-extended-count',
                                value: Uni.I18n.translate('readingtypesmanagement.addreadingtypes.defaultCountMsg', 'MTR', 'You are going to add 1 reading type. 1000 is the limit')
                            }
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
