Ext.define('Mdc.view.setup.deviceregisterconfiguration.numerical.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-numerical',
    itemId: 'deviceRegisterConfigurationDetail',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceregisterconfiguration.ValidationPreview'
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
                                items: [
                                    {
                                        xtype: 'fieldcontainer',
                                        flex: 1,
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
                                                xtype: 'fieldcontainer',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastReading', 'MDC', 'Last reading'),
                                                layout: 'hbox',
                                                items: [
                                                    {
                                                        xtype: 'displayfield',
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
                                                        return Ext.String.htmlEncode(value);
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.overflow.notspecified', 'MDC', 'Not specified')
                                                }
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                                        iconCls: 'x-uni-action-iconD',
                                        itemId: 'detailActionMenu',
                                        margin: '20 0 0 0',
                                        menu: {
                                            xtype: 'deviceRegisterConfigurationActionMenu'
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'deviceregisterdetailspreview-validation',
                                inputLabelWidth: 250,
                                router: me.router
                            }
                        ]
                    }

                ]
            }
        ];

        me.callParent(arguments);
    }
});