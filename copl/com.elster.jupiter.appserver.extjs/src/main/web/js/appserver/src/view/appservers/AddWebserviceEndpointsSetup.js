/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AddWebserviceEndpointsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-webservices-setup',
    overflowY: true,

    requires: [
        'Apr.view.appservers.AddWebserviceEndpointsGrid'
    ],

    content:[
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        xtype: 'add-webservices-grid',
                        itemId: 'add-webservices-grid',
                        store: 'Apr.store.UnservedWebserviceEndpoints',
                        plugins: {
                            ptype: 'bufferedrenderer'
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('validation.webserviceEndpoints.empty.title', 'APR', 'No web service endpoints found.'),
                        reasons: [
                            Uni.I18n.translate('validation.webserviceEndpoints.empty.list.item1', 'APR', 'No web service endpoints exist.'),
                            Uni.I18n.translate('dataExportTasks.webserviceEndpoints.empty.list.item3', 'APR', 'All web service endpoints have already been added to the application server.')
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
                            itemId: 'btn-add-webservices',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-add-webservices',
                            text: Uni.I18n.translate('general.cancel', 'APR', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }

            ]
        }
    ]
});