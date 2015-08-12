Ext.define('Mdc.view.setup.registerconfig.RegisterConfigEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerConfigEdit',
    itemId: 'registerConfigEdit',
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ReadingTypeDisplay'
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
                        xtype: 'combobox',
                        name: 'registerType',
                        fieldLabel: Uni.I18n.translate('registerConfig.registerType', 'MDC', 'Register type'),
                        itemId: 'registerTypeComboBox',
                        store: this.registerTypesOfDeviceType,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('registerConfig.selectRegisterType', 'MDC', 'Select a register type...'),
                        required: true,
                        forceSelection: true,
                        typeAhead: true,
                        width: 650,
                        //width: 320,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        name: 'readingType',
                        disabled: true
                    },
                    {
                        xtype: 'obis-field',
                        name: 'overruledObisCode',
                        itemId: 'editOverruledObisCodeField',
                        width: 450
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
                            {boxLabel: Uni.I18n.translate('registerConfig.number', 'MDC', 'Number'),itemId: 'numberRadio', inputValue: false, name: 'asText',checked: true},
                            {boxLabel: Uni.I18n.translate('registerConfig.text', 'MDC', 'Text'),itemId: 'textRadio', inputValue: true, name: 'asText'}
                        ]
                    },
                    {
                        xtype: 'numberfield',
                        name: 'numberOfDigits',
                        msgTarget: 'under',
                        required: true,
                        fieldLabel: Uni.I18n.translate('registerConfig.numberOfDigits', 'MDC', 'Number of digits'),
                        itemId: 'editNumberOfDigitsField',
                        maxValue: 20,
                        minValue: 0,
                        enforceMaxLength: true,
                        maxLength: 2,
                        width: 450
                        //width: 64
                    },
                    {
                        xtype: 'numberfield',
                        name: 'numberOfFractionDigits',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                        itemId: 'editNumberOfFractionDigitsField',
                        maxValue: 6,
                        minValue: 0,
                        maxLength: 1,
                        enforceMaxLength: true,
                        width: 450
                        //width: 64
                    },
                    {
                        xtype: 'numberfield',
                        name: 'overflow',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                        itemId: 'editOverflowValueField',
                        width: 450,
                        //width: 128,
                        maxValue: 100000000,
                        hideTrigger: true,
                        maxLength: 22,
                        enforceMaxLength: true,
                        required: true,
                        minValue: 1
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        itemId: 'overflowMsg',
                        items: [
                            {
                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerConfig.overflowValueInfo', 'MDC', 'The maximum overflow value is {0}.', ['1000000000']) + '</i></span>',
                                xtype: 'component',
                                itemId: 'overflowValueInfo'
                            }
                        ]
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