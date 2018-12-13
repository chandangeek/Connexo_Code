/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.AddWebServicesToState', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddWebServicesToState',
    requires: ['Dlc.devicelifecyclestates.view.AddWebServicesToStateGrid',
               'Dlc.devicelifecyclestates.store.AvailableWebServiceEndpoints'],
    overflowY: true,
    stateId: null,
    storeToUpdate: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('transitionBusinessProcess.addWebServiceEndpoints', 'DLC', 'Add web service endpoints'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'gridContainer',
                    selectByDefault: false,
                    grid: {
                        xtype: 'add-web-services-to-state-selection-grid',
                        itemId: 'add-web-services-grid',
                        store: 'Dlc.devicelifecyclestates.store.AvailableWebServiceEndpoints'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('webServiceEndpoints.empty.title', 'DLC', 'No web service endpoints found'),
                        reasons: [
                            Uni.I18n.translate('webServiceEndpoints.empty.list.item1', 'DLC', 'No web service endpoints are defined yet'),
                            Uni.I18n.translate('webServiceEndpoints.empty.list.item2', 'DLC', 'All web service endpoints are already added to the state.')
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
                            text: Uni.I18n.translate('general.add', 'DLC', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-web-service',
                            action: 'addWebService',
                            ui: 'action',
                            disabled: true
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-web-service',
                            text: Uni.I18n.translate('general.cancel', 'DLC', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
             ]
        }
    ],
    initComponent: function(){
        var me = this,
           availableWebServices = Ext.data.StoreManager.lookup('Dlc.devicelifecyclestates.store.AvailableWebServiceEndpoints');

        availableWebServices.getProxy().setExtraParam('stateId', me.stateId);
        me.callParent(arguments);
        if (availableWebServices) {
            availableWebServices.load(function(records, operation, success){
               if (success) {
                   if (me.storeToUpdate) {
                       var usedProcessesIds = Ext.Array.map(me.storeToUpdate.data.items, function(item){
                           return item.get("id");
                       });
                       availableWebServices.filterBy(function (record) {
                           return (Ext.Array.indexOf(usedProcessesIds, record.get("id")) === -1);
                       });
                       // filtering can result to empty grid: gridContainer acts on this by showing 'empty component'
                       me.down('#gridContainer').onChange(availableWebServices);
                       me.down('#btn-add-web-service').setVisible(availableWebServices.count() !== 0);
                   }
               } else {
                   console.warn("Available business processes could not be loaded");
                   // show gridContainer's 'empty component'
                   me.down('#gridContainer').onLoad(availableWebServices, records);
                   me.down('#btn-add-web-service').setVisible(false);
               }
            })
        }
    },
    getSelection: function(){
        var grid = this.down('#add-web-services-grid');
        return grid.getSelectionModel().getSelection();
    }
});