Ext.define('Mdc.view.setup.deviceregisterconfiguration.text.Preview', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralPreview',
    alias: 'widget.deviceRegisterConfigurationPreview-text',
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
                                fieldLabel: Uni.I18n.translate('general.nextReadingBlockStart', 'MDC', 'Next reading block start'),
                                name: 'reportedDateTime',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastValue', 'MDC', 'Last value'),
                                name: 'value'
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


