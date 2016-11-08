Ext.define('Mdc.view.setup.deviceregisterconfiguration.numerical.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-numerical',
    itemId: 'deviceRegisterConfigurationDetail',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceregisterconfiguration.ValidationPreview',
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
                itemId: 'deviceRegisterConfigurationDetailForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                                labelAlign: 'top',
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
                                        name: 'overruledOverflow',
                                        renderer: function (value) {
                                            if (!Ext.isEmpty(value)) {
                                                return Ext.String.htmlEncode(value);
                                            }

                                            return Uni.I18n.translate('deviceregisterconfiguration.overflow.notspecified', 'MDC', 'Not specified')
                                        }
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                        name: 'overruledNumberOfFractionDigits'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('channelConfig.useMultiplier', 'MDC', 'Use multiplier'),
                                        name: 'useMultiplier',
                                        renderer: function(value) {
                                            return value
                                                ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                                : Uni.I18n.translate('general.no', 'MDC', 'No');
                                        }
                                    },
                                    {
                                        xtype: 'deviceregisterdetailspreview-validation',
                                        inputLabelWidth: 250,
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
                            },
                            {
                                xtype: 'uni-button-action',
                                itemId: 'detailActionMenu',
                                margin: '20 0 0 0',
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