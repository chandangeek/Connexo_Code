Ext.define('Mdc.view.setup.deviceregisterconfiguration.billing.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-billing',
    itemId: 'deviceRegisterConfigurationPreview',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'deviceRegisterConfigurationPreviewForm',
                layout: 'form',
                items: [
                    {
                        xtype:'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
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
        ];

        me.callParent(arguments);
    }
});


