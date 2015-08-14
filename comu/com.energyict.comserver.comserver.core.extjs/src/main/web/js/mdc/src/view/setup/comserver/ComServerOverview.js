Ext.define('Mdc.view.setup.comserver.ComServerOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comServerOverview',
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
                    title: 'Overview',
                    flex: 1
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    privileges: Mdc.privileges.Communication.admin,
                    iconCls: 'x-uni-action-iconD',
                    menu: {
                        xtype: 'comserver-actionmenu',
                        itemId: 'comserverOverviewMenu'
                    }
                }
            ]
        },
        {
            xtype: 'form',
            itemId: 'comServerOverviewForm',
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
                            name: 'comServerType'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                            name: 'active',
                            renderer: function (val) {
                                val ? val = 'Active' : val = 'Inactive';
                                return val;
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
                            fieldLabel: Uni.I18n.translate('comserver.preview.storeTaskQueueSize', 'MDC', 'Store task queue size'),
                            name: 'storeTaskQueueSize'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.storeTaskThreadCount', 'MDC', 'Store task thread count'),
                            name: 'numberOfStoreTaskThreads'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.storeTaskQueuePriority', 'MDC', 'Store task queue priority'),
                            name: 'storeTaskThreadPriority'
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
                    serverId: me.serverId
                }
            ]
        };
        me.callParent(arguments)
    }
});

