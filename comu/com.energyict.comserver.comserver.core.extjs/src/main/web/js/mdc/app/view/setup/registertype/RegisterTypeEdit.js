Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',

    requires: [
        'Uni.form.field.Obis'
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
                        xtype: 'container',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'registerTypeEditForm',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    labelWidth: 250,
                                    width: 650
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
                                        xtype: 'textfield',
                                        name: 'readingType',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('registerType.mrid', 'MDC', 'Reading type'),
                                        emptyText: Uni.I18n.translate('registerType.selectReadingType', 'MDC', 'x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x'),
                                        itemId: 'editMrIdField',
                                        required: true,
                                        disabled: true
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
                                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerType.readingTypeInfo', 'MDC', 'Provide the values for the 18 attributes of the reading type, separated by a "."') + '</i></span>',
                                                xtype: 'component'
                                            }
                                        ]
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
