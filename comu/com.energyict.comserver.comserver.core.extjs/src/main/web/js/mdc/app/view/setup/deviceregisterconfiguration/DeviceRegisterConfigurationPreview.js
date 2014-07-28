Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceRegisterConfigurationPreview',
    itemId: 'deviceRegisterConfigurationPreview',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceRegisterConfigurationActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'deviceRegisterConfigurationPreviewForm',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    columnWidth: 0.5
                },
                items: [
                    {
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
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.multiplierMode', 'MDC', 'Multiplier mode'),
                                name: 'multiplierMode',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        return Uni.I18n.translate(value, 'MDC', value);
                                    }

                                    return Uni.I18n.translate('deviceregisterconfiguration.multiplierMode.notspecified', 'MDC', 'Not specified')
                                }
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastReading', 'MDC', 'Last reading date'),
                                name: 'lastReading',
                                format: 'M j, Y \\a\\t G:i',
                                renderer: function (value) {
                                    if(!Ext.isEmpty(value)) {
                                        return Ext.util.Format.date(value, this.format);
                                    }
                                    return Uni.I18n.translate('deviceregisterconfiguration.lastReading.notspecified', 'MDC', 'N/A');
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validationStatus', 'MDC', 'Validation status'),
                                name: 'validationStatus',
                                renderer: function (value) {
                                    if(value == true) {
                                        return 'OK';
                                    }
                                    return 'NOK';
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


