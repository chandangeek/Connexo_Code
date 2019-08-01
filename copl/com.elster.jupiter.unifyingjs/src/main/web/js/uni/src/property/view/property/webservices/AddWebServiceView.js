/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.webservices.AddWebServiceView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-web-service-view',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.property.view.property.webservices.AddWebServiceGrid'
    ],
    store: null,

    initComponent: function () {
        var me = this,
            webServices = Ext.getStore('Uni.property.store.PropertyWebServices');

        me.content = {
            xtype: 'panel',
            ui: 'large',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            title: Uni.I18n.translate('webService.addWebService', 'UNI', 'Add web services'),
            items: {
                xtype: 'emptygridcontainer',
                grid: {
                    xtype: 'add-web-service-grid',
                    itemId: 'add-web-service-grid',
                    store: me.store,
                    buttonAlign: 'left',
                    listeners: {
                        selectionchange: function (grid) {
                            var selection = grid.view.getSelectionModel().getSelection();
                            me.getAddButton().setDisabled(selection.length === 0);
                        }
                    }
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'add-web-service-grid-no-items-found-panel',
                    title: Uni.I18n.translate('webService.empty.deviceconfiguration.title', 'UNI', 'No web services found'),
                    reasons: [
                        Uni.I18n.translate('webService.empty.list.item1', 'UNI', 'No web services have been added yet.'),
                        Uni.I18n.translate('webService.empty.list.item2', 'UNI', 'Web services exist, but you do not have permission to view them.')
                    ]
                }
            },
            buttonAlign: 'left',
            buttons: [
                {
                    itemId: 'web-service-button-add',
                    text: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                    ui: 'action',
                    action: 'add',
                    disabled: true,
                    hidden: Ext.get('#add-web-service-grid-no-items-found-panel')
                },
                {
                    itemId: 'web-service-button-cancel',
                    text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                    ui: 'link',
                    action: 'cancel'
                }
            ]
        };
        me.callParent(arguments);

        // console.log(webServices.count())
        // Ext.getStore(me.store).load({
        //     scope: this,
        //     callback: function(records, operation, success) {
        //         debugger;
        //         if (success) {
        //             me.down('#web-service-button-add').setVisible(webServices.count() !== 0); 
        //             me.down('#web-service-button-cancel').setVisible(webServices.count() !== 0);
        //         }  else {
        //             me.down('#web-service-button-add').setVisible(false); 
        //     me.down('#web-service-button-cancel').setVisible(false);
        //         }
        //     }
        // })
    },

    getAddButton: function() {
        return this.down('button[action="add"]');
    },

    getCancelButton: function() {
        return this.down('button[action="cancel"]');
    }
});
