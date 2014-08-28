Ext.define('Mdc.view.setup.deviceregisterconfiguration.numerical.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-numerical',
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
                                html: '<h1>' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>',
                                itemId: 'deviceRegisterDetailTitle'
                            },
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
                                    type: 'column',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype:'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                                        labelAlign: 'top',
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
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestReading', 'MDC', 'Latest reading'),
                                                name: 'reportedDateTime',
                                                format: 'M j, Y \\a\\t G:i',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return Ext.util.Format.date(new Date(value), this.format);
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.latestReading.notspecified', 'MDC', '-')
                                                }
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                                                name: 'value'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfDigits', 'MDC', 'Number of digits'),
                                                name: 'numberOfDigits'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                                name: 'numberOfFractionDigits'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.overflow', 'MDC', 'Overflow'),
                                                name: 'overflow',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return value;
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.overflow.notspecified', 'MDC', 'Not specified')
                                                }
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.multiplier', 'MDC', 'Multiplier'),
                                                name: 'multiplier',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return value;
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.multiplier.notspecified', 'MDC', 'Not specified')
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

        me.callParent(arguments);
    }
});