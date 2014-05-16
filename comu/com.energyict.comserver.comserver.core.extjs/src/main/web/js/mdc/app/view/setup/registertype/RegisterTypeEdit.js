Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',

    requires: [

    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
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
                        margins: '10 10 10 10',
                        itemId: 'registerTypeEditCreateInformation'
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'registerTypeEditForm',
                                padding: '10 10 0 10',
                                layout: {
                                    type: 'vbox'
                                },
//                    tbar: [
//                        {
//                            xtype: 'component',
//                            html: '<b>Overview</b>',
//                            itemId: 'deviceTypePreviewTitle'
//                        }
//                    ],
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                                        itemId: 'editRegisterTypeNameField',
                                        width: 650,
                                        maxLength: 80,
                                        enforceMaxLength: true
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'obisCode',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('registerType.obisCode', 'MDC', 'OBIS code'),
                                        emptyText: Uni.I18n.translate('registerType.selectObisCode', 'MDC', 'x.x.x.x.x.x'),
                                        itemId: 'editObisCodeField',
                                        cls: 'obisCode',
                                        width: 650
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
                                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerType.obisCodeInfo', 'MDC', 'Provide the values for the 6 attributes of the Obis code, separated by a "."') + '</i></span>',
                                                xtype: 'component'

                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'combobox',
                                        name: 'unit',
                                        fieldLabel: Uni.I18n.translate('registerType.measurementUnit', 'MDC', 'Unit of measure'),
                                        itemId: 'measurementUnitComboBox',
                                        store: this.unitOfMeasure,
                                        queryMode: 'local',
                                        displayField: 'unit',
                                        valueField: 'unit',
                                        emptyText: Uni.I18n.translate('registerType.selectMeasurementUnit', 'MDC', 'Select a unit of measure...'),
                                        required: true,
                                        forceSelection: true,

                                        cls: 'obisCode',
                                        typeAhead: true,
                                        msgTarget: 'under',
                                        width: 650

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

                                        width: 650,
                                        typeAhead: true,
                                        msgTarget: 'under'
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'readingType',
                                        msgTarget: 'under',
                                        required: false,
                                        fieldLabel: Uni.I18n.translate('registerType.mrid', 'MDC', 'Reading type'),
                                        emptyText: Uni.I18n.translate('registerType.selectReadingType', 'MDC', 'x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x.x'),
                                        itemId: 'editMrIdField',
                                        required: true,
                                        readOnly: true,
                                        disabled: true,
                                        width: 650
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
                                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerType.readingTypeInfo', 'MDC', 'Provide the values for the 18 attributes of the reading type, separated by a "."') + '</i></span>',
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
                                                //  formBind: true
                                            },
                                            {
                                                xtype: 'component',
                                                padding: '3 0 0 10',
                                                itemId: 'cancelLink',
                                                autoEl: {
                                                    tag: 'a',
                                                    href: '#/administration/registertypes/',
                                                    html: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                                                }
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
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
    }

});
