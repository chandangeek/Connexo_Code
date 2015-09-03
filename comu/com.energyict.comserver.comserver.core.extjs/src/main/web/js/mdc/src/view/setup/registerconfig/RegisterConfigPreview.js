Ext.define('Mdc.view.setup.registerconfig.RegisterConfigPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.registerConfigPreview',
    itemId: 'registerConfigPreview',

    requires: [
        'Mdc.model.RegisterConfiguration',
        'Mdc.view.setup.registerconfig.RegisterConfigActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    title: Uni.I18n.translate('general.details','MDC','Details'),

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Mdc.privileges.DeviceType.admin,
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
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'reading-type-displayfield',
                                name: 'readingType'
                            },
                            {
                                xtype: 'obis-displayfield',
                                name: 'overruledObisCode',
                                fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code')
                            },
                            {
                                name: 'timeOfUse',
                                fieldLabel: Uni.I18n.translate('registerConfig.timeOfUse', 'MDC', 'Time of use')
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
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        itemId: 'registerConfigNumberPanel',
                        items: [
                            {
                                name: 'overflow',
                                fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value')
                            },
                            {
                                name: 'numberOfDigits',
                                fieldLabel: Uni.I18n.translate('registerConfig.numberOfDigits', 'MDC', 'Number of digits')
                            },
                            {
                                name: 'numberOfFractionDigits',
                                fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits')
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    updateRegisterConfig: function (registerConfig) {
        var me = this;

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(registerConfig);
        me.setTitle(registerConfig.get('name'));

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }
});
