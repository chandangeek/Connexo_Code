Ext.define('Mdc.view.setup.deviceregisterconfiguration.flags.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-flags',
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
                        tbar: [
                            {
                                xtype: 'component',
                                html: '<h1>' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>',
                                itemId: 'deviceRegisterDetailTitle'
                            },
                            {
                                xtype: 'component',
                                flex: 1
                            },
                            '->',
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                                iconCls: 'x-uni-action-iconD',
                                menu: {
                                    xtype: 'deviceRegisterConfigurationActionMenu'
                                }
                            }
                        ],

                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'column',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'container',
                                        columnWidth: 0.5,
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        defaults: {
                                            xtype: 'displayfield',
                                            labelWidth: 250
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
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.latestReading', 'MDC', 'Latest reading'),
                                                name: 'lastReading',
                                                format: 'M j, Y \\a\\t G:i',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value.reportedDateTime)) {
                                                        return Ext.util.Format.date(new Date(value.reportedDateTime), this.format);
                                                    }

                                                    return Uni.I18n.translate('deviceregisterconfiguration.latestReading.notspecified', 'MDC', '-')
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
                                                name: 'numberOfFractionDigits'
                                            }
                                        ]
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