Ext.define('Mdc.view.setup.registerconfig.RegisterConfigEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerConfigEdit',
    itemId: 'registerConfigEdit',

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
        this.down('#cancelLink').autoEl.href = returnLink;
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'registerConfigEditCreateTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'registerConfigEditForm',
                                padding: '10 10 0 10',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'combobox',
                                        name: 'registerMapping',
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
                                        msgTarget: 'under'
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('registerConfig.readingType', 'MDC', 'Reading type'),
                                        disabled: true,
                                        itemId: 'readingTypeContainer',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'mrid',
                                                itemId: 'create_mrid'
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'overruledObisCode',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code'),
                                        itemId: 'editOverruledObisCodeField',
                                        width: 450
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'obisCode',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code'),
                                        itemId: 'editObisCodeField',
                                        width: 450,
                                        hidden: true
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
                                    },
                                    {
                                        xtype: 'numberfield',
                                        name: 'overflowValue',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                                        itemId: 'editOverflowValueField',
                                        width: 450,
                                        maxValue: 100000000,
                                        hideTrigger: true,
                                        maxLength: 22,
                                        enforceMaxLength: true
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',

                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerConfig.overflowValueInfo', 'MDC', 'The maximum overflow value is {0}.', ['1000000000']) + '</i></span>',
                                                xtype: 'component',
                                                itemId: 'overflowValueInfo'
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'numberfield',
                                        name: 'multiplier',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('registerConfig.multiplier', 'MDC', 'Multiplier'),
                                        itemId: 'editMultiplierField',
                                        width: 450,
                                        hideTrigger: true
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',

                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerConfig.multiplierInfo', 'MDC', 'Multiplies the collected value.  The multiplied value will be stored in the register.') + '</i></span>',
                                                xtype: 'component'

                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',
                                        //width: 430,
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
                                                xtype: 'button',
                                                action: 'createAction',
                                                itemId: 'createEditButton'
//                                                        formBind: true
                                            },
                                            {
                                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                                xtype: 'button',
                                                ui: 'link',
                                                itemId: 'cancelLink',
                                                href: '#/setup/devicetypes/'
                                            }
                                        ]
                                    }
                                ]
                            }

                        ]
                    }


                ]
            }
        ];
        this.callParent(arguments);

        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editRegisterConfiguration';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterConfiguration';
        }
        this.down('#cancelLink').autoEl.href = this.returnLink;
    }
});