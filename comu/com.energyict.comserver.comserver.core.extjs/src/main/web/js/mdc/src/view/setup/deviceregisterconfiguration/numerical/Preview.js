Ext.define('Mdc.view.setup.deviceregisterconfiguration.numerical.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-numerical',
    itemId: 'deviceRegisterConfigurationPreview',
    router: null,

    requires: [
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
                                fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                                name: 'readingType'
                            },
                            {
                                xtype: 'reading-type-displayfield',
                                fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                                name: 'calculatedReadingType',
                                hidden: true
                            },
                            {
                                xtype: 'obis-displayfield',
                                name: 'obisCode'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                                name: 'multiplier',
                                hidden: true
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                                name: 'timeStamp',
                                renderer: function (value) {
                                    if (value) {
                                        var date = new Date(value);
                                        return Uni.DateTime.formatDateLong(date) + ' - ' + Uni.DateTime.formatTimeShort(date);
                                    }
                                    return '-';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                                name: 'value'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.overflowValue', 'MDC', 'Overflow value'),
                                name: 'overflow',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        return Ext.String.htmlEncode(value);
                                    }

                                    return Uni.I18n.translate('deviceregisterconfiguration.overflow.notspecified', 'MDC', 'Not specified')
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                name: 'numberOfFractionDigits'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('channelConfig.useMultiplier', 'MDC', 'Use multiplier'),
                                name: 'useMultiplier',
                                renderer: function(value) {
                                    return value
                                        ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                        : Uni.I18n.translate('general.no', 'MDC', 'No');
                                }
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


