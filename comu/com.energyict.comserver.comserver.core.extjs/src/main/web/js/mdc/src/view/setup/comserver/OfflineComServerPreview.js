/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comserver.OfflineComServerPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.offlineComServerPreview',
    requires: [
        'Mdc.store.ComServers',
        'Mdc.store.OfflineComServers',
        'Mdc.view.setup.comserver.ActionMenu'
    ],
    itemId: 'offlinecomserverpreview',
    layout: 'fit',
    title: Uni.I18n.translate('comserver.details', 'MDC', 'Details'),
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'comserver-actionmenu',
                itemId: 'offlineComServerViewMenu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'offlineComServerDetailsForm',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                        name: 'displayComServerType'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.serverLogLevel', 'MDC', 'Server log level'),
                        name: 'serverLogLevel'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.changesInterPollDelay', 'MDC', 'Changes inter poll delay'),
                        name: 'changesInterPollDelay',
                        renderer: function (val) {
                            val ? val = val.count + ' ' + val.timeUnit : null;
                            return val;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.onlineServer', 'MDC', 'Online ComServer'),
                        name: 'onlineComServerName'
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                        name: 'active',
                        renderer: function (val) {
                            if (val === true) {
                                return Uni.I18n.translate('general.active', 'MDC', 'Active');
                            } else {
                                return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.communicationLogLevel', 'MDC', 'Communication log level'),
                        name: 'communicationLogLevel'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.schedulingInterPollDelay', 'MDC', 'Scheduling inter poll delay'),
                        name: 'schedulingInterPollDelay',
                        renderer: function (val) {
                            val ? val = val.count + ' ' + val.timeUnit : null;
                            return val;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.communicationPortsLabel', 'MDC', 'Communication ports'),
                        name: 'comportslink',
                        htmlEncode: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('comserver.preview.serverName', 'MDC', 'Server name'),
                        name: 'serverName',
                        htmlEncode: false
                    }
                ]
            }
        ]
    }
});
