Ext.define('Mdc.view.setup.deviceregisterconfiguration.billing.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-billing',
    itemId: 'deviceRegisterConfigurationDetail',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceregisterconfiguration.DataLoggerSlaveHistory'
    ],
    showDataLoggerSlaveField: false,
    showDataLoggerSlaveHistory: false,

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
                                flex: 1,
                                items: [
                                    {
                                        xtype:'fieldcontainer',
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
                                                name: 'readingType'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                                                name: 'dataloggerSlavemRID',
                                                hidden: !me.showDataLoggerSlaveField,
                                                renderer: function(value) {
                                                    if (Ext.isEmpty(value)) {
                                                        return '-';
                                                    }
                                                    var href = me.router.getRoute('devices/device/registers').buildUrl({mRID: encodeURIComponent(value)});
                                                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                                                }
                                            },
                                            {
                                                xtype: 'obis-displayfield',
                                                name: 'overruledObisCode'
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('general.timestampLastValue', 'MDC', 'Timestamp last value'),
                                                name: 'timeStamp',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))])
                                                    }

                                                    return '-';
                                                }
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
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.interval', 'MDC', 'Interval'),
                                                name: 'interval',
                                                format: 'M j, Y \\a\\t G:i',
                                                renderer: function (value) {
                                                    if (!Ext.isEmpty(value)) {
                                                        return Ext.util.Format.date(new Date(value.start), this.format) + '-' + Ext.util.Format.date(new Date(value.end), this.format);
                                                    }

                                                    return '-';
                                                }
                                            },
                                            {
                                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.numberOfDigits', 'MDC', 'Number of digits'),
                                                name: 'numberOfDigits'
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

        if (me.showDataLoggerSlaveHistory) {
            me.on('afterrender', function() {
                me.down('#deviceRegisterConfigurationDetailForm').add(
                    {
                        xtype: 'dataLogger-slaveRegisterHistory'
                    }
                );
            }, me, {single:true});
        }

        me.callParent(arguments);
    }
});