Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Ext.container.Container',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',
    autoScroll: true,
    requires: [

    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',
    edit: false,
    isEdit: function () {
        return this.edit
    },
    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
        this.down('#cancelLink').autoEl.href = returnLink;
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
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
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
                                            type: 'vbox',
                                            align: 'stretch'
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
                                                fieldLabel: Uni.I18n.translate('registertype.obisCode', 'MDC', 'Obis code'),
                                                itemId: 'editObisCodeField',
                                                maxLength: 80,
                                                enforceMaxLength: true
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'measurementUnit',
                                                fieldLabel: Uni.I18n.translate('registertype.measurementUnit', 'MDC', 'Unit of measure'),
                                                itemId: 'measurementUnitComboBox',
                                                store: this.unitOfMeasure,
                                                queryMode: 'local',
                                                displayField: 'unit',
                                                valueField: 'unit',
                                                required: true,
                                                forceSelection: true,
                                                editable: false
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'timeOfUse',
                                                fieldLabel: Uni.I18n.translate('registertype.timeOfUse', 'MDC', 'Time of use'),
                                                itemId: 'timeOfUseComboBox',
                                                store: this.unitOfMeasure,
                                                queryMode: 'local',
                                                displayField: 'timeOfUse',
                                                valueField: 'timeOfUse',
                                                required: true,
                                                forceSelection: true,
                                                editable: false
                                            },
                                            {
                                                xtype: 'textfield',
                                                name: 'mrId',
                                                msgTarget: 'under',
                                                required: false,
                                                fieldLabel: Uni.I18n.translate('registertype.mrid', 'MDC', 'Reading type'),
                                                itemId: 'editDeviceTypeNameField',
                                                required: true,
                                                readOnly: true
                                            },
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                validator: function (currentValue) {
                                                    if (currentValue.length > 0) {
                                                        return true;
                                                    } else {
                                                        return Uni.I18n.translate('devicetype.emptyName', 'MDC', 'The name of a device type can not be empty.')
                                                    }
                                                },
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                                                itemId: 'editRegisterTypeNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'measurementKind',
                                                fieldLabel: Uni.I18n.translate('registertype.measurementKind', 'MDC', 'Type'),
                                                itemId: 'measurementKindComboBox',
                                                store: this.measurementKinds,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'name',
                                                required: false,
                                                forceSelection: true,
                                                editable: false
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
                                                        itemId: 'createEditButton',
                                                        formBind: true
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


                ]
            }
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
        this.down('#cancelLink').autoEl.href = this.returnLink;

    }


});
