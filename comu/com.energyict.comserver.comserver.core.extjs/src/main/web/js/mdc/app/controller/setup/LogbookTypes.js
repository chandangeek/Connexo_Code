Ext.define('Mdc.controller.setup.LogbookTypes', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookTypes'
    ],

    views: [
        'setup.devicetype.DeviceTypeLogbooks',
        'setup.devicetype.ActionMenu',
        'Isu.view.ext.button.GridAction'
    ],

    refs: [
        {
            ref: 'deviceTypeLogbooks',
            selector: 'device-type-logbooks'
        }
    ],

    init: function () {
        var me = this;
        me.control({
            'device-type-logbooks grid': {
                itemclick: me.loadGridItemDetail
            },
            'device-type-logbooks grid uni-actioncolumn': {
                menuclick : me.deleteLogbookType
            }
        });
        me.store = me.getStore('Mdc.store.LogbookTypes');
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('device-type-logbooks panel[name=details]')[0],
            itemForm = Ext.ComponentQuery.query('device-type-logbooks form[name=logbookTypeDetails]')[0],
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        itemForm.loadRecord(record);
        itemPanel.setTitle(record.get('name'));
        itemPanel.show();
        preloader.destroy();
    },

    deleteLogbookType: function () {
        var self = this,
            logbooksView = self.getDeviceTypeLogbooks(),
            grid = logbooksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/dtc/devicetypes/' + logbooksView.deviceTypeId + '/logbooktypes/' + record.data.id,
            itemPanel = Ext.ComponentQuery.query('device-type-logbooks panel[name=details]')[0],
            confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        text: 'Remove',
                        ui: 'delete',
                        handler: function () {
                            var preloader = Ext.create('Ext.LoadMask', {
                                msg: "Loading...",
                                target: confirmMessage
                            });
                            preloader.show();
                            Ext.Ajax.request({
                                url: url,
                                method: 'DELETE',
                                success: function (response) {
                                    confirmMessage.close();
                                    itemPanel.hide();

                                    Ext.create('widget.uxNotification', {
                                        html: 'Successfully removed',
                                        ui: 'notification-success'
                                    }).show();

                                    self.store.load({
                                            callback: function () {
                                                var numberOfLogbooksContainer = Ext.ComponentQuery.query('device-type-logbooks toolbar container[name=LogBookCount]')[0],
                                                    gridView = grid.getView(),
                                                    selectionModel = gridView.getSelectionModel(),
                                                    count = [
                                                        {
                                                            xtype: 'label',
                                                            text: self.store.getCount() + ' logbook type(s)'
                                                        },
                                                        {
                                                           xtype: 'container',
                                                           flex: 1
                                                        }
                                                    ];
                                                numberOfLogbooksContainer.removeAll(true);
                                                numberOfLogbooksContainer.add(count);
                                                if (self.store.getCount() < 1) {
                                                    grid.hide();
                                                    grid.next().show();
                                                } else {
                                                    grid.getStore().load({
                                                        callback: function () {
                                                            selectionModel.select(0);
                                                            grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    );
                                },
                                failure: function (response) {
                                    confirmMessage.close();
                                    var result;
                                    if (response != null) {
                                        result = Ext.decode(response.responseText, true);
                                    }
                                    if (result !== null) {
                                        Ext.widget('messagebox', {
                                            buttons: [
                                                {
                                                    text: 'Close',
                                                    action: 'cancel',
                                                    handler: function(btn){
                                                        btn.up('messagebox').hide()
                                                    }
                                                }
                                            ]
                                        }).show({
                                            ui: 'notification-error',
                                            title: result.error,
                                            msg: result.message,
                                            icon: Ext.MessageBox.ERROR
                                        })

                                    } else {
                                        Ext.widget('messagebox', {
                                            buttons: [
                                                {
                                                    text: 'Close',
                                                    action: 'cancel',
                                                    handler: function(btn){
                                                        btn.up('messagebox').hide()
                                                    }
                                                }
                                            ]
                                        }).show({
                                            ui: 'notification-error',
                                            title: 'Failed to delete ' + record.data.name,
                                            msg: 'The device type could not be deleted. There was a problem accessing the database',
                                            icon: Ext.MessageBox.ERROR
                                        })
                                    }
                                },
                                callback: function () {
                                    preloader.destroy();
                                }
                            });
                        }
                    },
                    {
                        text: 'Cancel',
                        ui: 'link',
                        handler: function () {
                            confirmMessage.close();
                        }
                    }
                ]
            });
        confirmMessage.show({
            title: 'Remove logbook type ' + record.data.name + ' ?',
            msg: '<p>The logbook type will no longer be available on this device type.</p>',
            icon: Ext.MessageBox.WARNING,
            cls: 'isu-delete-message'
        });
    }
});


