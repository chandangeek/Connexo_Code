Ext.define('Mdc.view.setup.deviceregisterconfiguration.flags.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-flags',
    itemId: 'deviceRegisterConfigurationDetail',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ObisDisplay'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: 'fit',
                items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceRegisterConfigurationDetailForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        tbar: [
                            {
                                xtype: 'component',
                                flex: 1
                            },
                            '->',
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                                iconCls: 'x-uni-action-iconD',
                                itemId: 'detailActionMenu',
                                menu: {
                                    xtype: 'deviceRegisterConfigurationActionMenu'
                                }
                            }
                        ],

                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        xtype:'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                                        labelAlign: 'top',
                                        flex: 1,
                                        layout: 'vbox',
                                        defaults: {
                                            xtype: 'displayfield',
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.name', 'MDC', 'Name'),
                                                name: 'name'
                                            },
                                            {
                                                xtype: 'obis-displayfield',
                                                name: 'obisCode'
                                            },
                                            {
                                                xtype: 'reading-type-displayfield',
                                                name: 'readingType'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastReading', 'MDC', 'Last reading'),
                                                name: 'reportedDateTime',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return Uni.DateTime.formatDateLong(new Date(value))
                                                            + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                                                            + Uni.DateTime.formatTimeLong(new Date(value));
                                                    }

                                                    return '-';
                                                }
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastValue', 'MDC', 'Last value'),
                                                name: 'value'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfDigits', 'MDC', 'Number of digits'),
                                                name: 'numberOfDigits'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                                name: 'numberOfFractionDigits'
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                                        iconCls: 'x-uni-action-iconD',
                                        itemId: 'detailActionMenu',
                                        menu: {
                                            xtype: 'deviceRegisterConfigurationActionMenu'
                                        }
                                    }
                                ]
                            }
                        ]
                    }

                ]
            }
        ];

        me.callParent(arguments);
    }
});