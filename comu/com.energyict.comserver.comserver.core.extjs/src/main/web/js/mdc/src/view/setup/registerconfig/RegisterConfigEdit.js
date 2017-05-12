/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registerconfig.RegisterConfigEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerConfigEdit',
    itemId: 'registerConfigEdit',
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ReadingTypeCombo',
        'Uni.view.form.ComboBoxWithEmptyComponent'
    ],
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterConfiguration';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createRegisterConfiguration';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'form',
                itemId: 'registerConfigEditForm',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        width: 650
                    },
                    {
                        xtype: 'comboboxwithemptycomponent',
                        fieldLabel: Uni.I18n.translate('registerConfig.registerType', 'MDC', 'Register type'),
                        itemId: 'registerTypeComboBox',
                        config: {
                            name: 'registerType',
                            store: this.registerTypesOfDeviceType,
                            queryMode: 'local',
                            displayField: 'name',
                            noObjectsText: Uni.I18n.translate('registerConfig.noRegisterTypesAvailable', 'MDC', 'No register types available'),
                            valueField: 'id',
                            emptyText: Uni.I18n.translate('registerConfig.selectRegisterType', 'MDC', 'Select a register type...'),
                            required: true,
                            allowBlank: false,
                            forceSelection: true,
                            editable: false,
                            width: 650,
                            msgTarget: 'under'
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'obis-code-container',
                        required: true,
                        width: 450,
                        layout: 'hbox',
                        fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code'),
                        items: [
                            {
                                xtype: 'obis-field',
                                name: 'overruledObisCode',
                                itemId: 'editOverruledObisCodeField',
                                fieldLabel: '',
                                required: false,
                                afterSubTpl: null,
                                width: 150
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'mdc-restore-obiscode-btn',
                                hidden: false,
                                disabled: true
                            }
                        ]
                    },
                    {
                        xtype: 'obis-field',
                        name: 'obisCode',
                        itemId: 'editObisCodeField',
                        width: 450,
                        hidden: true
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'valueTypeRadioGroup',
                        fieldLabel: Uni.I18n.translate('registerConfig.storeValueAs', 'MDC', 'Store value as'),
                        columns: 1,
                        defaults: {
                            name: 'asText'
                        },
                        allowBlank: false,
                        required: true,
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('registerConfig.number', 'MDC', 'Number'),
                                itemId: 'numberRadio',
                                inputValue: false,
                                checked: true
                            },
                            {
                                boxLabel: Uni.I18n.translate('registerConfig.text', 'MDC', 'Text'),
                                itemId: 'textRadio',
                                inputValue: true
                            }
                        ]
                    },
                    {
                        xtype: 'numberfield',
                        name: 'overflow',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                        itemId: 'editOverflowValueField',
                        width: 450,
                        hideTrigger: true,
                        maxLength: 15, // don't increase this value. Javascript can't handle precise values larger than 9007199254740992
                        enforceMaxLength: true,
                        required: true,
                        allowBlank: false,
                        minValue: 1
                    },
                    {
                        xtype: 'numberfield',
                        name: 'numberOfFractionDigits',
                        required: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                        itemId: 'editNumberOfFractionDigitsField',
                        minValue: 0,
                        maxValue: 6,
                        maxLength: 1,
                        enforceMaxLength: true,
                        width: 450
                    },
                    {
                        xtype: 'checkboxfield',
                        fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                        itemId: 'mdc-register-config-multiplier-checkbox',
                        name: 'useMultiplier',
                        boxLabel: Uni.I18n.translate('registerConfig.useMultiplier', 'MDC', 'Use multiplier'),
                        checked: false,
                        disabled: true
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        itemId: 'mdc-collected-readingType-field',
                        fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                        name: 'collectedReadingType',
                        hidden: true
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        itemId: 'mdc-calculated-readingType-field',
                        fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                        name: 'calculatedReadingType',
                        hidden: true
                    },
                    {
                        xtype: 'reading-type-combo',
                        itemId: 'mdc-calculated-readingType-combo',
                        fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                        required: true,
                        width: 650,
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'createAction',
                                itemId: 'createEditButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: '#/administration/devicetypes/'
                            }
                        ]
                    }
                ]
            }
        ];
        this.callParent(arguments);

        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterConfiguration';
            this.down('#registerTypeComboBox').disable();
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createRegisterConfiguration';
        }
        this.down('#cancelLink').href = this.returnLink;
    }
});