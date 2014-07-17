Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterConfigurationDetail',
    itemId: 'deviceRegisterConfigurationDetail',

    mRID: null,
    registerId: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationActionMenu',
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationMenu'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceRegisterConfigurationMenu',
                        itemId: 'stepsMenu',
                        mRID: me.mRID,
                        registerId: me.registerId,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceRegisterConfigurationDetailForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
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
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.name', 'MDC', 'Name'),
                                                name: 'name'
                                            },
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.obiscode', 'MDC', 'OBIS code'),
                                                name: 'obisCode'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.readingType', 'MDC', 'Reading type'),
                                                layout: {
                                                    type: 'hbox',
                                                    align: 'stretch'
                                                },
                                                items: [
                                                    {
                                                        xtype: 'displayfield',
                                                        name: 'mrid',
                                                        itemId: 'preview_mrid'
                                                    },
                                                    {
                                                        xtype: 'component',
                                                        html: '&nbsp;&nbsp;'
                                                    },
                                                    {
                                                        xtype: 'button',
                                                        icon: '../ext/packages/uni-theme-skyline/resources/images/icon-info-small.png',
                                                        tooltip: Uni.I18n.translate('deviceregisterconfiguration.readingType.tooltip', 'MDC', 'Reading type info'),
                                                        cls: 'uni-btn-transparent',
                                                        handler: function () {
                                                            var record = me.down('#deviceRegisterConfigurationDetailForm').form.getRecord();
                                                            this.fireEvent('showReadingTypeInfo', record);
                                                        },
                                                        itemId: 'readingTypeBtn',
                                                        action: 'showReadingTypeInfo'
                                                    }

                                                ]
                                            },
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfDigits', 'MDC', 'Number of digits'),
                                                name: 'numberOfDigits'
                                            },
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                                name: 'numberOfFractionDigits'
                                            },
                                            {
                                                xtype: 'displayfield',
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
                                                xtype: 'displayfield',
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