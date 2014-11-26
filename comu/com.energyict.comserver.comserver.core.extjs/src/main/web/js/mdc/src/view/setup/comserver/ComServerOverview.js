Ext.define('Mdc.view.setup.comserver.ComServerOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comServerOverview',
    requires: [
        'Mdc.view.setup.comserver.ActionMenu',
        'Mdc.view.setup.comserver.SubMenu'
    ],
    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('comserver.title.communicationServers', 'MDC', 'Communication servers'),
        width: 300,
        items: [{
            xtype: 'comserversubmenu',
            itemId: 'comserversubmenu'
        }]
    },
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
                    hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.communicationInfrastructure'),
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
                            fieldLabel: Uni.I18n.translate('comserver.preview.name', 'MDC', 'Name'),
                            name: 'name'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.type', 'MDC', 'Type'),
                            name: 'comServerType'
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
                            fieldLabel: Uni.I18n.translate('comserver.preview.status', 'MDC', 'Status'),
                            name: 'active',
                            renderer: function (val) {
                                val ? val = 'Active' : val = 'Inactive';
                                return val;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('comserver.preview.communicationLogLevel', 'MDC', 'Communication log level'),
                            name: 'communicationLogLevel'
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
                            fieldLabel: Uni.I18n.translate('comserver.preview.communicationPortsLabel', 'MDC', 'Communication ports'),
                            name: 'comportslink'
                        }
                    ]
                }
            ]
        }
    ]
});

