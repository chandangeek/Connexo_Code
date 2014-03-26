Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',
    autoScroll: true,
    requires: [

    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-container',
    edit: false,
    isEdit: function () {
        return this.edit
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
        this.items = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'breadcrumbTrail',
                        region: 'north',
                        padding: 6
                    },
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'registerTypeEditCreateTitle',
                        margins: '10 10 10 10'
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
//                            html: '<h4>Overview</h4>',
//                            itemId: 'deviceTypePreviewTitle'
//                        }
//                    ],
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'obisCode',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('registerType.obisCode', 'MDC', 'OBIS code'),
                                        emptyText: Uni.I18n.translate('registerType.selectObisCode', 'MDC', 'x.x.x.x.x.x'),
                                        itemId: 'editObisCodeField',
                                        maxLength: 80,
                                        enforceMaxLength: true,
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
                                                html: '<span style="color: grey"><i>' +Uni.I18n.translate('registerType.obisCodeInfo','MDC','Provide this value for the 6 attributes of the Obis code.  Devide each value with a "."')  + '</i></span>',
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
                                        emptyText: Uni.I18n.translate('registerType.selectMeasurementUnit', 'MDC', 'Select unit of measure'),
                                        required: true,
                                        forceSelection: true,
                                        editable: false,
                                        cls: 'obisCode',
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
                                        emptyText: Uni.I18n.translate('registerType.selectTimeOfUse', 'MDC', 'Select time of use'),
                                        required: true,
                                        forceSelection: true,
                                        editable: false,
                                        width: 650
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'mrid',
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
                                                html: '<span style="color: grey"><i>' + Uni.I18n.translate('registerType.readingTypeInfo','MDC','Provide this value for the 18 attributes of the reading type.  Devide each value with a "."') + '</i></span>',
                                                xtype: 'component'

                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                                        itemId: 'editRegisterTypeNameField',
                                        maxLength: 80,
                                        enforceMaxLength: true,
                                        width: 650
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
                                                    href: '#setup/registertypes/',
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
