Ext.define('Mdc.view.setup.deviceregisterconfiguration.text.Detail', {
    extend: 'Mdc.view.setup.deviceregisterconfiguration.GeneralDetail',
    alias: 'widget.deviceRegisterConfigurationDetail-text',
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