Ext.define('Mdc.view.setup.deviceregisterconfiguration.text.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-text',
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
                                items: [
                                    {
                                        xtype:'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                                        labelAlign: 'top',
                                        flex: 1,
                                        layout: 'vbox',
                                        defaults: {
                                            xtype: 'displayfield',
                                            labelWidth: 250
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
                                                name: 'overruledObisCode'
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
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'custom-attribute-sets-placeholder-form',
                                        itemId: 'custom-attribute-sets-placeholder-form-id',
                                        actionMenuXtype: 'deviceRegisterConfigurationActionMenu',
                                        attributeSetType: 'register',
                                        router: me.router
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