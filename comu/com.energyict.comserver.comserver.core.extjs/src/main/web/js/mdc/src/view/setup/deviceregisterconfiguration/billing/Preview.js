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
    layout: 'column',
    defaults: {
        columnWidth: 0.5
    },

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
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
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
                                fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                                name: 'timeStamp',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.nextReadingBlockStart', 'MDC', 'Next reading block start'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'reportedDateTime',
                                        renderer: function (value) {
                                            return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                                        }
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
                            }
                        ]
                    },
                    {
                        xtype: 'deviceregisterdetailspreview-validation',
                        router: me.router
                    }
                ]
            },
            {
                xtype: 'custom-attribute-sets-placeholder-form',
                itemId: 'custom-attribute-sets-placeholder-form-id',
                actionMenuXtype: 'deviceRegisterConfigurationActionMenu',
                attributeSetType: 'register',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});


