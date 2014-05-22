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
        this.control({
            'device-type-logbooks grid': {
                itemclick: this.loadGridItemDetail
            },
            'device-type-logbooks grid uni-actioncolumn': {
                remove: this.deleteLogbookType
            }
        });
        this.store = this.getStore('Mdc.store.LogbookTypes');
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

    showDatabaseError: function(msges) {
        var self = this,
            logbooksView = self.getDeviceTypeLogbooks();
        self.getApplication().fireEvent('isushowmsg', {
            type: 'error',
            msgBody: msges,
            y: 10,
            closeBtn: true,
            btns: [
                {
                    text: 'Cancel',
                    cls: 'isu-btn-link',
                    hnd: function () {
                        window.location = '#/administration/devicetypes/' + logbooksView.deviceTypeId + '/logbooktypes';
                    }
                }
            ],
            listeners: {
                close: {
                    fn: function () {
                        logbooksView.enable();
                    }
                }
            }
        });
        logbooksView.disable();
    },

    deleteLogbookType: function () {
        var self = this,
            logbooksView = self.getDeviceTypeLogbooks(),
            grid = logbooksView.down('grid'),
            header = {
                style: 'msgHeaderStyle'
            },
            bodyItem = {},
            msges = [],
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
                                success: function () {
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
                                                    count = Ext.widget('container', {
                                                        html: self.store.getCount() + ' logbook type(s)'
                                                    });
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
                                    var result = Ext.decode(response.responseText, true);
                                    if (result !== null) {
                                        header.text = result.message;
                                        msges.push(header);
                                        bodyItem.style = 'msgItemStyle';
                                        bodyItem.text = result.error;
                                        msges.push(bodyItem);
                                        self.getApplication().fireEvent('isushowmsg', {
                                            type: 'error',
                                            msgBody: msges,
                                            y: 10,
                                            closeBtn: true,
                                            btns: [
                                                {
                                                    text: 'Cancel',
                                                    cls: 'isu-btn-link',
                                                    hnd: function () {
                                                        window.location = '#/administration/devicetypes/' + logbooksView.deviceTypeId + '/logbooktypes';
                                                    }
                                                }
                                            ],
                                            listeners: {
                                                close: {
                                                    fn: function () {
                                                        logbooksView.enable();
                                                    }
                                                }
                                            }
                                        });
                                        logbooksView.disable();
                                    }
                                    else {
                                        header.text = 'Error during removing';
                                        msges.push(header);
                                        bodyItem.style = 'msgItemStyle';
                                        bodyItem.text = 'The logbook type could not be removed because of an error in the database.';
                                        msges.push(bodyItem);
                                        self.showDatabaseError(msges);
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


