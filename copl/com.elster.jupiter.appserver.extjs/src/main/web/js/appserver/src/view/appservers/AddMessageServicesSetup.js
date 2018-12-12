/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AddMessageServicesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-message-services-setup',
    overflowY: true,

    requires: [
        'Apr.view.appservers.AddMessageServicesGrid'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        itemId: 'addMessageServicesGrid',
                        xtype: 'add-message-services-grid',
                        itemId: 'add-message-services-grid',
                        store: 'Apr.store.UnservedMessageServices',
                        plugins: {
                            ptype: 'bufferedrenderer'
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('validation.messageServices.empty.title', 'APR', 'No message services found.'),
                        reasons: [
                            Uni.I18n.translate('validation.messageServices.empty.list.item1', 'APR', 'No message services have been added yet.'),
                            Uni.I18n.translate('validation.messageServices.empty.list.item2', 'APR', 'No message services comply with the filter.'),
                            Uni.I18n.translate('dataExportTasks.messageServices.empty.list.item3x', 'APR', 'All message services have already been added to the export task.')
                        ]
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'APR', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-message-services',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-add-message-services',
                            action: 'test',
                            text: Uni.I18n.translate('general.cancel', 'APR', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ]
});
