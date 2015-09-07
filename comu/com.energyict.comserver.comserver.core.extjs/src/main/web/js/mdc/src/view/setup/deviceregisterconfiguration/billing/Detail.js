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
                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox'
                                },
                                flex: 1,
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
                                                xtype: 'reading-type-displayfield',
                                                name: 'readingType'
                                            },
                                            {
                                                xtype: 'obis-displayfield',
                                                name: 'obisCode'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.timestampLastValue', 'MDC', 'Timestamp last value'),
                                                name: 'timeStamp',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))]).toLowerCase()
                                                    }

                                                    return '-';
                                                }
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastReading', 'MDC', 'Last reading'),
                                                layout: 'hbox',
                                                items: [
                                                    {
                                                        xtype: 'displayfield',
                                                        name: 'reportedDateTime',
                                                        renderer: function (value) {
                                                            if (!Ext.isEmpty(value)) {
                                                                returnUni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))]).toLowerCase()
                                                            }
                                                            return '-';
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
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastValue', 'MDC', 'Last value'),
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

                                                    return '-';
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
                                                xtype: 'deviceregisterdetailspreview-validation',
                                                inputLabelWidth: 250,
                                                router: me.router
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
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