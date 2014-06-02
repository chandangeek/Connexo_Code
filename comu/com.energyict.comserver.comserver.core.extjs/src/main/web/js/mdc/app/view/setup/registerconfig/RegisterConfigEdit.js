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
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'form',
                itemId: 'registerConfigEditForm',
                ui: 'large',
                //width: '100%',
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
                        width: 320,
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
                        width: 256
                    },
                    {
                        xtype: 'textfield',
                        name: 'obisCode',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code'),
                        itemId: 'editObisCodeField',
                        width: 256,
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
                        width: 64
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
                        width: 64
                    },
                    {
                        xtype: 'numberfield',
                        name: 'overflowValue',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                        itemId: 'editOverflowValueField',
                        width: 128,
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
                        width: 64,
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
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editRegisterConfiguration';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterConfiguration';
        }
        this.down('#cancelLink').href = this.returnLink;
    }
});