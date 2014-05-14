Ext.define('Mdc.view.setup.registerconfig.RegisterConfigPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.registerConfigPreview',
    itemId: 'registerConfigPreview',
    requires: [
        'Mdc.model.RegisterConfiguration'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: 'Details',
    tools: [
        {
            xtype: 'button',
            icon: '../mdc/resources/images/actionsDetail.png',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            menu: [
                {
                    text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                    itemId: 'editRegisterConfig',
                    action: 'editRegisterConfig'

                },
                {
                    xtype: 'menuseparator'
                },
                {
                    text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                    itemId: 'deleteRegisterConfig',
                    action: 'deleteRegisterConfig'

                }
            ]
        }

    ],

    deviceTypeId: null,
    deviceConfigId: null,
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                border: false,
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('registerConfig.noRegisterConfigSelected', 'MDC', 'No register configuration selected') + '</h4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        height: '100px',
                        html: '<h5>' + Uni.I18n.translate('registerConfig.selectRegisterConfig', 'MDC', 'Select a register configuration to see its details') + '</h5>'
                    }
                ]
            },


            {
                xtype: 'form',
                border: false,
                itemId: 'registerConfigPreviewForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('registerConfig.deviceRegister', 'MDC', 'Device register'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('registerConfig.readingType', 'MDC', 'Reading type'),
                                        labelAlign: 'right',
                                        labelWidth: 150,
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'mrid',
                                                itemId: 'preview_mrid'
                                            },
                                            {
                                                xtype: 'component',
                                                html: '&nbsp;&nbsp;'
                                            },
                                            {
                                                xtype: 'button',
                                                icon: '../mdc/resources/images/information.png',
                                                tooltip: 'Reading type info',
                                                cls: 'uni-btn-transparent',
                                                handler: function (item, test) {
                                                    var record = me.down('#registerConfigPreviewForm').form.getRecord();
                                                    this.fireEvent('showReadingTypeInfo', record);
                                                },
                                                itemId: 'raadingTypeBtn',
                                                action: 'showReadingTypeInfo'
                                            }

                                        ]
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'obisCode',
                                        fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'overruledObisCode',
                                        fieldLabel: Uni.I18n.translate('registerConfig.overruledObisCode', 'MDC', 'Overruled OBIS code'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'unitOfMeasure',
                                        fieldLabel: Uni.I18n.translate('registerConfig.unit', 'MDC', 'Unit of measure'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'timeOfUse',
                                        fieldLabel: Uni.I18n.translate('registerConfig.timeOfUse', 'MDC', 'Time of use'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'overflowValue',
                                        fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'numberOfDigits',
                                        fieldLabel: Uni.I18n.translate('registerConfig.numberOfDigits', 'MDC', 'Number of digits'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'numberOfFractionDigits',
                                        fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'multiplier',
                                        fieldLabel: Uni.I18n.translate('registerConfig.multiplier', 'MDC', 'Multiplier'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    }
                                ]
                            }
                        ]
                    }

                ]
            }
        ];

        this.callParent(arguments);
    }
});