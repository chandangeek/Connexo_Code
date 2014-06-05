Ext.define('Mdc.controller.setup.LogbookConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookConfigurations'
    ],

    views: [
        'setup.deviceconfiguration.DeviceConfigurationLogbooks',
        'setup.deviceconfiguration.EditLogbookConfiguration',
        'setup.deviceconfiguration.ActionMenu'
    ],

    refs: [
        {
            ref: 'deviceConfigurationLogbooks',
            selector: 'device-configuration-logbooks'
        }
    ],

    init: function () {
        this.control({
            'device-configuration-logbooks grid': {
                itemclick: this.loadGridItemDetail
            },
            'device-configuration-logbooks grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'device-logbook-action-menu': {
                click: this.chooseAction
            }

        });
        this.store = this.getStore('Mdc.store.LogbookConfigurations');
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('device-configuration-logbooks panel[name=details]')[0],
            itemForm = Ext.ComponentQuery.query('device-configuration-logbooks form[name=logbookConfigurationDetails]')[0],
            overruledField = itemForm.down('[name=overruledObisCode]'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        this.logbookConfigId = record.internalId;
        itemForm.loadRecord(record);
        if (record.data.overruledObisCode == record.data.obisCode) {
            overruledField.setValue('-');
        }
        itemPanel.setTitle(record.get('name'));
        itemPanel.show();
        preloader.destroy();
    },

    chooseAction: function (menu, item) {
        var action = item.action,
            logbooksView = this.getDeviceConfigurationLogbooks();
        switch (action) {
            case 'edit':
                window.location.href = '#/administration/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations/' + this.logbookConfigId + '/edit';
                break;
            case 'delete':
                this.deleteLogbookType();
                break;
        }
    },

    deleteLogbookType: function () {
        var self = this,
            logbooksView = self.getDeviceConfigurationLogbooks(),
            grid = logbooksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/dtc/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations/' + record.data.id,
            itemPanel = Ext.ComponentQuery.query('device-configuration-logbooks panel[name=details]')[0],
            confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        text: 'Delete',
                        ui: 'remove',
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
                                    preloader.destroy();
                                    itemPanel.hide();

                                    Ext.create('widget.uxNotification', {
                                        html: 'Successfully deleted',
                                        ui: 'notification-success'
                                    }).show();

                                    self.store.load({
                                            callback: function () {
                                                var numberOfLogbooksContainer = Ext.ComponentQuery.query('device-configuration-logbooks toolbar container[name=LogBookCount]')[0],
                                                    gridView = grid.getView(),
                                                    selectionModel = gridView.getSelectionModel(),
                                                    count = Ext.widget('container', {
                                                        html: self.store.getCount() + ' logbook configuration(s)'
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
                                    preloader.destroy();
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
                                            title: 'Failed to udelete ' + record.data.name,
                                            msg: 'Logbook configuration could not be deleted. There was a problem accessing the database',
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
            })
            ;
        confirmMessage.show({
            title: 'Delete logbook configuration ' + record.data.name + ' ?',
            msg: '<p>The logbook configuration will no longer be available on this device type.</p>',
            icon: Ext.MessageBox.QUESTION,
            cls: 'isu-delete-message'
        });
    }
})
;
