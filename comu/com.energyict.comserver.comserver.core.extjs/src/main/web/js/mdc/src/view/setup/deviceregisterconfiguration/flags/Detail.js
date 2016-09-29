Ext.define('Mdc.view.setup.deviceregisterconfiguration.flags.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-flags',
    itemId: 'deviceRegisterConfigurationDetail',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceregisterconfiguration.DataLoggerSlaveHistory'
    ],
    showDataLoggerSlaveField: false,
    showDataLoggerSlaveHistory: false,
    dataLoggerSlaveHistoryStore: null,

    initComponent: function () {
        var me = this;

        me.content = [
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
                        flex: 1
                    },
                    '->',
                    {
                        xtype: 'uni-button-action',
                        itemId: 'detailActionMenu',
                        menu: {
                            xtype: 'deviceRegisterConfigurationActionMenu'
                        }
                    }
                ],

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
                                        name: 'readingType'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                                        name: 'dataloggerSlaveName',
                                        hidden: !me.showDataLoggerSlaveField,
                                        renderer: function(value) {
                                            if (Ext.isEmpty(value)) {
                                                return '-';
                                            }
                                            var href = me.router.getRoute('devices/device/registers').buildUrl({deviceId: encodeURIComponent(value)});
                                            return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                                        }
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
                            },
                            {
                                xtype: 'custom-attribute-sets-placeholder-form',
                                itemId: 'custom-attribute-sets-placeholder-form-id',
                                actionMenuXtype: 'deviceRegisterConfigurationActionMenu',
                                attributeSetType: 'register',
                                router: me.router
                            },
                            {
                                xtype: 'uni-button-action',
                                itemId: 'detailActionMenu',
                                menu: {
                                    xtype: 'deviceRegisterConfigurationActionMenu'
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        if (me.showDataLoggerSlaveHistory) {
            me.on('afterrender', function() {
                me.down('#deviceRegisterConfigurationDetailForm').add(
                    {
                        xtype: 'dataLogger-slaveRegisterHistory',
                        dataLoggerSlaveHistoryStore: me.dataLoggerSlaveHistoryStore
                    }
                );
            }, me, {single:true});
        }

        me.callParent(arguments);
    }
});