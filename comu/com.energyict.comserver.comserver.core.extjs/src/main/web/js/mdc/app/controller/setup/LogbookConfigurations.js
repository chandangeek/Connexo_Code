Ext.define('Mdc.controller.setup.LogbookConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookConfigurations'
    ],

    views: [
        'setup.deviceconfiguration.DeviceConfigurationLogbooks',
        'setup.deviceconfiguration.EditLogbookConfiguration',
        'setup.deviceconfiguration.ActionMenu',
        'Isu.view.ext.button.GridAction'
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
            header = {
                style: 'msgHeaderStyle'
            },
            bodyItem = {},
            msges = [],
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/dtc/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations/' + record.data.id,
            itemPanel = Ext.ComponentQuery.query('device-configuration-logbooks panel[name=details]')[0],
            confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        text: 'Delete',
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
                                    var result = Ext.decode(response.responseText);

                                    if (result !== null) {
                                        Ext.Msg.show({
                                            title: result.error,
                                            msg: result.message,
                                            icon: Ext.MessageBox.WARNING,
                                            buttons: Ext.MessageBox.CANCEL,
                                            ui: 'notification-error'
                                        });
                                    }
                                    else {
                                        Ext.Msg.show({
                                            title: 'Error during deleting',
                                            msg: 'The logbook configuration could not be deleted because of an error in the database.',
                                            icon: Ext.MessageBox.WARNING,
                                            buttons: Ext.MessageBox.CANCEL,
                                            ui: 'notification-error'
                                        });
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
            title: 'Delete logbook configuration ' + record.data.name + ' ?',
            msg: '<p>The logbook configuration will no longer be available on this device type.</p>',
            icon: Ext.MessageBox.QUESTION,
            cls: 'isu-delete-message'
        });
    }
});
