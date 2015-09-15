Ext.define('Mdc.view.setup.deviceregisterconfiguration.flags.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-flags',
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
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastReading', 'MDC', 'Last reading'),
                                name: 'reportedDateTime',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        return Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))]).toLowerCase()
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
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


