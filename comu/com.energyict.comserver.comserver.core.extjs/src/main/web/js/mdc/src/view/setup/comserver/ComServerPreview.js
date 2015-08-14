Ext.define('Mdc.view.setup.comserver.ComServerPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerPreview',
    requires: [
        'Mdc.store.ComServers'
    ],
    itemId: 'comserverpreview',
    layout: 'fit',
    title: Uni.I18n.translate('comserver.details', 'MDC', 'Details'),
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Mdc.privileges.Communication.admin,
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'comserver-actionmenu',
                itemId: 'comserverViewMenu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'comServerDetailsForm',
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
                            val ? val = 'Active' : val = 'Inactive';
                            return val;
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
                    }
                ]
            }
        ]
    }

});
