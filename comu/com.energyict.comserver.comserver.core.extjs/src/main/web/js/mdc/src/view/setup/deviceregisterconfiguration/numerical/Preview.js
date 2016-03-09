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
                                renderer: function(value){
                                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
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
                                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                                        }
                                    }
                                ]

                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastValue', 'MDC', 'Last value'),
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


