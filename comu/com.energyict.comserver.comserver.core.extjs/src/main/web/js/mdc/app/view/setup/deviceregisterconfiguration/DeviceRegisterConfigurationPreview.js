Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceRegisterConfigurationPreview',
    itemId: 'deviceRegisterConfigurationPreview',
    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationActionMenu'
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
                            labelAlign: 'right',
                            labelWidth: 200
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
                                        icon: '../mdc/resources/images/info.png',
                                        tooltip: Uni.I18n.translate('deviceregisterconfiguration.readingType.tooltip', 'MDC', 'Reading type info'),
                                        cls: 'uni-btn-transparent',
                                        handler: function () {
                                            var record = me.down('#deviceRegisterConfigurationPreviewForm').form.getRecord();
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
                                renderer: function(value) {
                                    if(!Ext.isEmpty(value)) {
                                        return value;
                                    }

                                    return Uni.I18n.translate('deviceregisterconfiguration.overflow.notspecified', 'MDC', 'Not specified')
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.multiplierMode', 'MDC', 'Multiplier mode'),
                                name: 'multiplierMode',
                                renderer: function(value) {
                                    if(!Ext.isEmpty(value)) {
                                        return Uni.I18n.translate(value, 'MDC', value);
                                    }

                                    return Uni.I18n.translate('deviceregisterconfiguration.multiplierMode.notspecified', 'MDC', 'Not specified')
                                }
                            }
                        ]
                    },
                    {
                        defaults: {
                            labelAlign: 'right',
                            labelWidth: 200
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.lastInterval', 'MDC', 'Last interval'),
                                renderer: function(value) {
                                    return 'TBD';
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validationStatus', 'MDC', 'Validation status'),
                                renderer: function(value) {
                                    return 'TBD';
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


