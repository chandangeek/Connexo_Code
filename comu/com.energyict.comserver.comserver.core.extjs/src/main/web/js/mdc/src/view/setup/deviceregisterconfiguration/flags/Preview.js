Ext.define('Mdc.view.setup.deviceregisterconfiguration.flags.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-flags',
    itemId: 'deviceRegisterConfigurationPreview',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
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
                                name: 'overruledObisCode'
                            },
                            {
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
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfDigits', 'MDC', 'Number of digits'),
                                name: 'numberOfDigits'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                name: 'overruledNumberOfFractionDigits'
                            }
                        ]
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


