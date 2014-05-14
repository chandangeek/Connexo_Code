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
            'device-configuration-logbooks grid actioncolumn': {
                click: this.showItemAction
            },
            'device-logbook-action-menu': {
                beforehide: this.hideItemAction,
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
                window.location.href = '#setup/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations/' + this.logbookConfigId + '/edit';
                break;
            case 'delete':
                this.deleteLogbookType();
                break;
        }
    },

    showItemAction: function (grid, cell, rowIndex, colIndex, e, record) {
        var cellEl = Ext.get(cell);
        this.hideItemAction();
        this.gridActionIcon = cellEl.first();
        this.gridActionIcon.hide();
        this.gridActionIcon.setHeight(0);
        this.gridActionBtn = Ext.create('widget.grid-action', {
            renderTo: cell,
            menu: {
                xtype: 'device-logbook-action-menu',
                logbookId: record.data.id
            }
        });
        this.gridActionBtn.showMenu();
    },

    hideItemAction: function () {
        if (this.gridActionBtn) {
            this.gridActionBtn.destroy();
        }
        if (this.gridActionIcon) {
            this.gridActionIcon.show();
            this.gridActionIcon.setHeight(22);
        }
    },

    showDatabaseError: function(msges) {
        var self = this,
            logbooksView = self.getDeviceConfigurationLogbooks();
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
                        window.location = '#setup/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations';
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
                                    header.text = 'Successfully deleted';
                                    self.getApplication().fireEvent('isushowmsg', {
                                        type: 'notify',
                                        msgBody: [header],
                                        y: 10,
                                        showTime: 5000
                                    });
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
                                                        window.location = '#setup/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations';
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
                                        header.text = 'Error during deleting';
                                        msges.push(header);
                                        bodyItem.style = 'msgItemStyle';
                                        bodyItem.text = 'The logbook configuration could not be deleted because of an error in the database.';
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
                        cls: 'isu-btn-link',
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
