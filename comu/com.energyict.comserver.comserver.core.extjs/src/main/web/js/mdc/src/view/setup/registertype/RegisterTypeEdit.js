Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',

    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
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
                        itemId: 'registerTypeEditCreateTitle'
                    },

                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'registerTypeEditCreateInformation',
                        margin: '0 0 20 0',
                        hidden: true
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'registerTypeEditForm',
                        defaults: {
                            labelWidth: 200,
                            width: 700
                        },
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'name',
                                msgTarget: 'under',
                                required: true,
                                fieldLabel: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                                itemId: 'editRegisterTypeNameField',
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'obis-field',
                                itemId: 'editObisCodeField',
                                name: 'obisCode'
                            },
                            {
                                xtype: 'combobox',
                                name: 'unit',
                                fieldLabel: Uni.I18n.translate('registerType.measurementUnit', 'MDC', 'Unit of measure'),
                                itemId: 'measurementUnitComboBox',
                                store: this.unitOfMeasure,
                                queryMode: 'local',
                                displayField: 'localizedValue',
                                valueField: 'id',
                                emptyText: Uni.I18n.translate('registerType.selectMeasurementUnit', 'MDC', 'Select a unit of measure...'),
                                required: true,
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under',
                                cls: 'obisCode'
                            },
                            {
                                xtype: 'combobox',
                                name: 'timeOfUse',
                                fieldLabel: Uni.I18n.translate('registerType.timeOfUse', 'MDC', 'Time of use'),
                                itemId: 'timeOfUseComboBox',
                                store: this.timeOfUse,
                                queryMode: 'local',
                                displayField: 'timeOfUse',
                                valueField: 'timeOfUse',
                                emptyText: Uni.I18n.translate('registerType.selectTimeOfUse', 'MDC', 'Select a time of use...'),
                                required: true,
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'reading-type-combo',
                                name: 'readingType',
                                itemId: 'readingTypeCombo',
                                store: 'AvailableReadingTypesForRegisterType',
                                disabled: true,
                                required: true,
                                allowBlank: false,
                                submitValue: false
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'noReadingAvailable',
                                hidden: true,
                                fieldLabel: Uni.I18n.translate('registerType.mrid', 'MDC', 'Reading type'),
                                value:  Uni.I18n.translate('registerType.noReadingAvailable', 'MDC', 'No reading types available for selected unit of measure and time of use'),
                                required: true
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp;',
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
                                        xtype: 'button',
                                        ui: 'link',
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        itemId: 'cancelLink',
                                        href: '#/administration/registertypes/'
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
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
        this.down('#cancelLink').href = this.returnLink;
    }

});
