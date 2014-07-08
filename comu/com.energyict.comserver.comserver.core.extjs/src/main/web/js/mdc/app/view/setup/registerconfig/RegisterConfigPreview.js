Ext.define('Mdc.view.setup.registerconfig.RegisterConfigPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.registerConfigPreview',
    itemId: 'registerConfigPreview',

    requires: [
        'Mdc.model.RegisterConfiguration',
        'Mdc.view.setup.registerconfig.RegisterConfigActionMenu'
    ],

    title: 'Details',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'register-config-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        this.items = [
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
                                fieldLabel: Uni.I18n.translate('registerConfig.deviceRegister', 'MDC', 'Register type'),
                                labelAlign: 'right',
                                labelWidth: 250
                            },
                            {
                                xtype: 'fieldcontainer',
                                columnWidth: 0.5,
                                fieldLabel: Uni.I18n.translate('registerConfig.readingType', 'MDC', 'Reading type'),
                                labelAlign: 'right',
                                labelWidth: 250,
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
                                        icon: '../mdc/resources/images/info.png',
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
                                labelWidth: 250
                            },
                            {
                                xtype: 'displayfield',
                                name: 'overruledObisCode',
                                fieldLabel: Uni.I18n.translate('registerConfig.overruledObisCode', 'MDC', 'Overruled OBIS code'),
                                labelAlign: 'right',
                                labelWidth: 250
                            },
                            {
                                xtype: 'displayfield',
                                name: 'unitOfMeasure',
                                fieldLabel: Uni.I18n.translate('registerConfig.unit', 'MDC', 'Unit of measure'),
                                labelAlign: 'right',
                                labelWidth: 250
                            },
                            {
                                xtype: 'displayfield',
                                name: 'timeOfUse',
                                fieldLabel: Uni.I18n.translate('registerConfig.timeOfUse', 'MDC', 'Time of use'),
                                labelAlign: 'right',
                                labelWidth: 250
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
                                name: 'overflow',
                                fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                                labelAlign: 'right',
                                labelWidth: 250
                            },
                            {
                                xtype: 'displayfield',
                                name: 'numberOfDigits',
                                fieldLabel: Uni.I18n.translate('registerConfig.numberOfDigits', 'MDC', 'Number of digits'),
                                labelAlign: 'right',
                                labelWidth: 250
                            },
                            {
                                xtype: 'displayfield',
                                name: 'numberOfFractionDigits',
                                fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                labelAlign: 'right',
                                labelWidth: 250
                            },
                            {
                                xtype: 'displayfield',
                                name: 'multiplier',
                                fieldLabel: Uni.I18n.translate('registerConfig.multiplier', 'MDC', 'Multiplier'),
                                labelAlign: 'right',
                                labelWidth: 250
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    updateRegisterConfig: function(registerConfig) {
        this.loadRecord(registerConfig);
        this.setTitle(registerConfig.get('name'));
        this.down('#preview_mrid').setValue(registerConfig.getReadingType().get('mrid'));
    }
});
