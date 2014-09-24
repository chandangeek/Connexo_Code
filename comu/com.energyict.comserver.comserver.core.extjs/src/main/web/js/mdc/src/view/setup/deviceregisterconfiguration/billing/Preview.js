Ext.define('Mdc.view.setup.deviceregisterconfiguration.billing.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-billing',
    itemId: 'deviceRegisterConfigurationPreview',
    router: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Mdc.view.setup.deviceregisterconfiguration.ValidationPreview'
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
                            labelWidth: 200
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
                                name: 'timeStamp',
                                format: 'M j, Y \\a\\t G:i',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        return Ext.util.Format.date(new Date(value), this.format);
                                    }

                                    return Uni.I18n.translate('deviceregisterconfiguration.latestMeasurement.notspecified', 'MDC', '-')
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestReading', 'MDC', 'Latest reading'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'reportedDateTime',
                                        renderer: function (value) {
                                            if (!Ext.isEmpty(value)) {
                                                return Ext.util.Format.date(new Date(value), this.format);
                                            }

                                            return Uni.I18n.translate('deviceregisterconfiguration.latestReading.notspecified', 'MDC', '-')
                                        }
                                    },
                                    {
                                        xtype: 'button',
                                        tooltip: Uni.I18n.translate('deviceregisterconfiguration.tooltip.latestReading', 'MDC', 'The moment when the data was read out for the last time'),
                                        iconCls: 'icon-info-small',
                                        ui: 'blank',
                                        itemId: 'latestReadingHelp',
                                        shadow: false,
                                        margin: '6 0 0 10',
                                        width: 16
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                                name: 'value'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.interval', 'MDC', 'Interval'),
                                name: 'interval',
                                format: 'M j, Y \\a\\t G:i',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        return Ext.util.Format.date(new Date(value.start), this.format) + '-' + Ext.util.Format.date(new Date(value.end), this.format);
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
                    },
                    {
                        xtype: 'deviceregisterdetailspreview-validation',
                        router: me.router
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


