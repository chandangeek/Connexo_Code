Ext.define('Mdc.controller.setup.LogbookTypesOverview', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Mdc.store.Logbook'
    ],

    views: [
        'setup.logbooktype.LogbookTypesOverview',
        'setup.logbooktype.List',
        'setup.logbooktype.Item',
        'setup.logbooktype.ActionMenu',
        'setup.logbooktype.DockedButtons',
        'setup.logbooktype.EmptyListMessage'
    ],

    init: function () {
        this.control({
            'logbook-overview logbook-list': {
                afterrender: this.loadStore,
                itemclick: this.loadGridItemDetail
            },
            'logbook-overview logbook-list uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'logbook-overview logbook-item logbook-action-menu': {
                click: this.chooseAction
            }
        });

        this.listen({
            store: {
                '#Mdc.store.Logbook': {
                    load: this.checkLogBookTypesCount
                }
            }
        });

        this.store = this.getStore('Mdc.store.Logbook');
    },

    loadStore: function () {
        this.store.load();
    },

    checkLogBookTypesCount: function () {
        var numberOfLogbooksContainer = Ext.ComponentQuery.query('logbook-overview logbook-docked-buttons container[name=LogBookCount]')[0],
            grid = Ext.ComponentQuery.query('logbook-overview logbook-list')[0];
        if (grid) {
            var gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                widget = Ext.widget('container', {
                    html: this.store.getCount() + ' logbook type(s)'
                });

            numberOfLogbooksContainer.removeAll(true);
            numberOfLogbooksContainer.add(widget);

            if (this.store.getCount() < 1) {
                grid.hide();
                grid.next().show();
            } else {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('logbook-overview logbook-item')[0],
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.getData().id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.getData().id;
        itemPanel.fireEvent('change', itemPanel, record);
        var itemForm = itemPanel.down('form');
        itemForm.loadRecord(record);
        itemPanel.down().setTitle(record.get('name'));
        itemPanel.down('logbook-action-menu').record = record;
        preloader.destroy();
    },

    showOverview: function () {
        var widget = Ext.widget('logbook-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        switch (action) {
            case 'edit':
                window.location.href = '#/administration/logbooktypes/edit/' + menu.record.getId();
                break;
            case 'delete':
                this.removeLogbook(menu.record.getId());
                break;
        }
    },

    removeLogbook: function (logBookId) {
        var self = this,
            overview = Ext.ComponentQuery.query('logbook-overview')[0],
            record = overview.down('form').getRecord(),
            logBookStore = self.store;
        self.lastLogBookIdToRemove = logBookId;
        if (!Ext.isEmpty(self.lastLogBookIdToRemove)) {
            var logBookId = self.lastLogBookIdToRemove,
                confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        xtype: 'button',
                        text: 'Remove',
                        ui: 'delete',
                        handler: function () {
                            var preloader = Ext.create('Ext.LoadMask', {
                                msg: "Loading...",
                                target: confirmMessage
                            });
                            preloader.show();
                            Ext.Ajax.request({
                                url: '/api/mds/logbooktypes/' + logBookId,
                                method: 'DELETE',
                                success: function () {
                                    confirmMessage.close();
                                    Ext.create('widget.uxNotification', {
                                        html: 'Successfully removed',
                                        ui: 'notification-success'
                                    }).show();
                                    logBookStore.load();
                                },
                                failure: function (response) {
                                    confirmMessage.close();
                                    var result;
                                    if (response != null) {
                                        result = Ext.decode(response.responseText, true);
                                    }
                                    if (result !== null) {
                                        var msgWindow = Ext.widget('messagebox', {
                                            buttons: [
                                                {
                                                    text: 'Cancel',
                                                    action: 'cancel',
                                                    ui: 'link',
                                                    handler: function (me) {
                                                        me.up('messagebox').close();
                                                    }
                                                }
                                            ]
                                        });
                                        msgWindow.show({
                                            ui: 'notification-error',
                                            title: 'Failed to remove ' + record.data.name,
                                            msg: result.message,
                                            icon: Ext.MessageBox.ERROR
                                        });
                                    } else {
                                        self.showDatabaseError(record);
                                    }
                                },
                                callback: function () {
                                    preloader.destroy();
                                }
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        text: 'Cancel',
                        ui: 'link',
                        handler: function () {
                            confirmMessage.close();
                        }
                    }
                ]
            });
            confirmMessage.show({
                title: 'Remove ' + record.data.name + ' ?',
                msg: '<p>This logbook will no longer be available.</p>',
                icon: Ext.MessageBox.WARNING
            });
        }
    },

    showDatabaseError: function (record) {
        var msgWindow = Ext.widget('messagebox', {
            buttons: [
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    handler: function (me) {
                        me.up('messagebox').close();
                    }
                }
            ]
        });
        msgWindow.show({
            ui: 'notification-error',
            title: 'Failed to remove ' + record.data.name,
            msg: 'The logbook could not be removed because of an error in the database.',
            icon: Ext.MessageBox.ERROR
        });
    }

});