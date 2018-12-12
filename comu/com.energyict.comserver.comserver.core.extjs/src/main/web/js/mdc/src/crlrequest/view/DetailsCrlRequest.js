/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.view.DetailsCrlRequest', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.crl-request-details',

    itemId: 'crl-request-details',

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'crl-request-details-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'crl-request-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                xtype: 'displayfield',
                                itemId: 'crl-task-name',
                                name: 'task'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.securityAccessor', 'MDC', 'Security accessor'),
                                xtype: 'displayfield',
                                itemId: 'crl-security-accessor',
                                name: 'securityAccessor'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('crlrequest.caName', 'MDC', 'CA name'),
                                itemId: 'crl-ca-name',
                                name: 'caName'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.logLevel', 'MDC', 'Log level'),
                                xtype: 'displayfield',
                                itemId: 'crl-log-level',
                                name: 'logLevel'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('crlrequest.nextRun', 'MDC', 'Next run'),
                                itemId: 'crl-next-run',
                                name: 'nextRun',
                                listeners: {
                                    afterrender: function(item) {
                                        var record = item.up('form').getRecord();
                                        item.setValue(Uni.DateTime.formatDateTimeLong(new Date(record.get('nextRun'))));

                                    }
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('crlrequest.requestFrequency', 'MDC', 'Request frequency'),
                                itemId: 'crl-request-frequency',
                                name: 'periodicalExpressionInfo',
                                listeners: {
                                    afterrender: function(item) {
                                        var record = item.up('form').getRecord(),
                                            timeUnit = record.get('periodicalExpressionInfo').timeUnit;
                                        item.setValue(record.get('periodicalExpressionInfo').count + ' ' + timeUnit.slice(0, --timeUnit.length) + '(s)');
                                    }
                                }
                            }
                        ]
                    }
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
                    itemId: 'crl-request-details-action-menu',
                    privileges: function () {
                        return me.canAdministrate;
                    },
                    menu: me.actionMenu
                }
            ]
        };
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        itemId: 'crl-request-details-side-menu',
                        title: Uni.I18n.translate('general.crlRequest', 'MDC', 'CRL Request'),
                        objectType: Uni.I18n.translate('general.crlRequest', 'MDC', 'CRL Request'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                                itemId: 'crl-request-details-overview-link'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;
        me.down('#crl-request-details-form').loadRecord(record);
    },

    setRecurrentTasks: function (itemId, recurrentTasks) {
        var me = this,
            recurrentTaskList = [];

        Ext.isArray(recurrentTasks) && Ext.Array.each(recurrentTasks, function (recurrentTask) {
            recurrentTaskList.push('- ' + Ext.htmlEncode(recurrentTask.name));
        });
        me.down(itemId).setValue((recurrentTaskList.length == 0) ? recurrentTaskList = '-' : recurrentTaskList.join('<br/>'));
    }
});