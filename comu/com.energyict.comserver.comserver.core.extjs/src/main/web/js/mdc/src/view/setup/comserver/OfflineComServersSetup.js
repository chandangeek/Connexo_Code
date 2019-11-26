/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comserver.OfflineComServersSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.offlineComServersSetup',

    requires: [
        'Mdc.view.setup.comserver.OfflineComServersGrid',
        'Mdc.view.setup.comserver.OfflineComServerPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: {
        ui: 'large',
        title: Uni.I18n.translate('general.offline.comServers', 'MDC', 'Mobile communication servers'),
        items: [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'offlineComServersGrid'
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-comservers',
                    title: Uni.I18n.translate('comserver.empty.title', 'MDC', 'No communication servers found'),
                    reasons: [
                        Uni.I18n.translate('comserver.empty.list.item1', 'MDC', 'No communication servers created yet')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('comServer.addOffline', 'MDC', 'Add mobile communication server'),
                            itemId: 'btn-no-items-add-offline-communication-server',
                            privileges: Mdc.privileges.Communication.admin,
                            action: 'addOfflineComServer',
                            href: '#/administration/offlinecomservers/add/offline'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'offlineComServerPreview'
                }
            }
        ]}

});

