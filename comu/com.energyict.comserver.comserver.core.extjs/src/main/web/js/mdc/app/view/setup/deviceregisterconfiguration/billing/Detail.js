Ext.define('Mdc.view.setup.deviceregisterconfiguration.billing.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-billing',
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
                                        xtype: 'container',
                                        columnWidth: 0.5,
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
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
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestMeasurement', 'MDC', 'Latest measurement'),
                                                name: 'lastReading',
                                                format: 'M j, Y \\a\\t G:i',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value.timeStamp)) {
                                                        return Ext.util.Format.date(new Date(value.timeStamp), this.format);
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.latestMeasurement.notspecified', 'MDC', '-')
                                                }
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestReading', 'MDC', 'Latest reading'),
                                                name: 'lastReading',
                                                format: 'M j, Y \\a\\t G:i',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value.reportedDateTime)) {
                                                        return Ext.util.Format.date(new Date(value.reportedDateTime), this.format);
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.latestReading.notspecified', 'MDC', '-')
                                                }
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                                                name: 'value'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.interval', 'MDC', 'Interval'),
                                                name: 'lastReading',
                                                format: 'M j, Y \\a\\t G:i',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value.interval)) {
                                                        return Ext.util.Format.date(new Date(value.interval.start), this.format) + '-' + Ext.util.Format.date(new Date(value.interval.end), this.format);
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.interval.notspecified', 'MDC', '-')
                                                }
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