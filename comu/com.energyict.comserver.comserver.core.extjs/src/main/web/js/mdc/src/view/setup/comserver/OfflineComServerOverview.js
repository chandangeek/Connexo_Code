/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comserver.OfflineComServerOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.offlineComServerOverview',
    requires: [
        'Mdc.view.setup.comserver.ActionMenu',
        'Mdc.view.setup.comserver.SideMenu'
    ],
    serverId: null,
    content: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    ui: 'large',
                    itemId: 'comserver-overview-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1
                },
                {
                    xtype: 'uni-button-action',
                    menu: {
                        xtype: 'comserver-actionmenu',
                        itemId: 'offlineComServerOverviewMenu'
                    }
                }
            ]
        },
        {
            xtype: 'form',
            itemId: 'offlineComServerOverviewForm',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 1
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
                            fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                            name: 'active',
                            renderer: function (val) {
                                if (val) {
                                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                                } else {
                                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                }
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.serverLogLevel', 'MDC', 'Server log level'),
                            name: 'serverLogLevel'
                        },

                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.communicationLogLevel', 'MDC', 'Communication log level'),
                            name: 'communicationLogLevel'
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
                            fieldLabel: Uni.I18n.translate('comserver.preview.shedulingInterPollDelay', 'MDC', 'Sheduling inter poll delay'),
                            name: 'schedulingInterPollDelay',
                            renderer: function (val) {
                                val ? val = val.count + ' ' + val.timeUnit : null;
                                return val;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.serverName', 'MDC', 'Server name'),
                            name: 'serverName',
                            htmlEncode: false
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.onlineServer', 'MDC', 'Online ComServer'),
                            name: 'onlineComServerName',
                            htmlEncode: false
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.communicationPortsLabel', 'MDC', 'Communication ports'),
                            htmlEncode: false,
                            name: 'comportslink'
                        }
                    ]
                }
            ]
        }
    ],
    initComponent: function () {
        var me = this;
        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'comserversidemenu',
                    itemId: 'comserversidemenu',
                    serverId: me.serverId,
                    isOfflineServer : function(){
                        return true;
                    }
                }
            ]
        };
        me.callParent(arguments)
    }
});

