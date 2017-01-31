/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AddImportServicesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-import-services-setup',
    overflowY: true,

    requires: [
      'Apr.view.appservers.AddImportServicesGrid'
    ],

    content:[
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        itemId: 'addImportServicesGrid',
                        xtype: 'add-import-services-grid',
                        itemId: 'add-import-services-grid',
                        store: 'Apr.store.UnservedImportServices',
                        plugins: {
                            ptype: 'bufferedrenderer'
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('validation.importServices.empty.title', 'APR', 'No import services found.'),
                        reasons: [
                            Uni.I18n.translate('validation.importServices.empty.list.item1', 'APR', 'No import services exist.'),
                            Uni.I18n.translate('dataExportTasks.importServices.empty.list.item3', 'APR', 'All import services have already been added to the application server.')
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
                            itemId: 'btn-add-import-services',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-add-import-services',
                            text: Uni.I18n.translate('general.cancel', 'APR', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }

            ]
        }
    ]
});