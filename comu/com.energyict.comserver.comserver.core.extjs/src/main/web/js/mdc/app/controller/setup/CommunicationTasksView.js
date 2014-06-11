Ext.define('Mdc.controller.setup.CommunicationTasksView', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Mdc.store.CommunicationTasks'
    ],

    views: [
        'Mdc.view.setup.comtasks.ComtaskSetup',
        'Mdc.view.setup.comtasks.ComtaskGrid',
        'Mdc.view.setup.comtasks.ComtaskPreview'
    ],

    refs: [
        {
            ref: 'tasksView',
            selector: 'comtaskSetup'
        },
        {
            ref: 'itemPanel',
            selector: 'comtaskPreview'
        },
        {
            ref: 'tasksGrid',
            selector: 'comtaskGrid'
        },

        {
            ref: 'nameField',
            selector: '#tasksName'
        },
        {
            ref: 'commandsField',
            selector: '#tasksCommands'
        },
        {
            ref: 'emptyPanel',
            selector: '#emptyPanel'
        },
        {
            ref: 'rulesGridPagingToolbarTop',
            selector: 'comtaskGrid pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'comtaskSetup comtaskGrid': {
                select: this.showTaskDetails
            },
            'comtaskSetup comtaskGrid gridview': {
                afterrender: this.onCommunicationTasksGridRefresh
            },
            'comtaskSetup comtaskGrid uni-actioncolumn': {
                menuclick: this.chooseCommunicationTasksAction
            },
            'comtaskActionMenu': {
                click: this.chooseCommunicationTasksAction
            }
        });
        this.store = this.getStore('Mdc.store.CommunicationTasks');
    },

    showCommunicationTasksView: function () {
        var widget = Ext.widget('comtaskSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseCommunicationTasksAction: function (menu, item) {
        var self = this,
            action = item.action;

        switch (action) {
            case 'edit':
                window.location.href = '#/administration/communicationtasks/' + self.taskId;
                break;
            case 'delete':
                this.deleteTask(self.taskId);
                break;
        }
    },

    onCommunicationTasksGridRefresh: function () {
        var self = this,
            tasksGrid = self.getTasksGrid(),
            itemPanel = self.getItemPanel(),
            selectionModel = tasksGrid.getView().getSelectionModel();
        self.store.load({
            callback: function () {
                if (this.getTotalCount() < 1) {
                    tasksGrid.hide();
                    itemPanel.hide();
                    self.getEmptyPanel().show();
                } else {
                    tasksGrid.getView().getSelectionModel().select(0);
                    self.showTaskDetails(tasksGrid.getView(), selectionModel.getLastSelected());
                }
            }
        })
    },

    showTaskDetails: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            nameField = this.getNameField(),
            commandsField = this.getCommandsField(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.view.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        this.taskId = record.data.id;
        Ext.Ajax.request({
            url: '/api/cts/comtasks/' + record.data.id,
            success: function (response) {
                var rec = Ext.decode(response.responseText),
                    str = '';
                itemPanel.setTitle(rec.name);
                nameField.setValue(rec.name);
                Ext.Array.each(rec.commands, function (command) {
                    str += command.action.charAt(0).toUpperCase() + command.action.slice(1) + ' ' + command.category.charAt(0).toUpperCase() + command.category.slice(1) + '<br/>';
                });
                commandsField.setValue(str);
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showDatabaseError: function () {
        var self = this,
            msgWindow = Ext.widget('messagebox', {
                itemId: 'msgWindowRemove',
                buttons: [
                    {
                        text: 'Cancel',
                        action: 'cancel',
                        ui: 'link',
                        handler: function (me) {
                            me.up('#msgWindowRemove').close();
                            window.location = '#/administration/communicationtasks';
                        }
                    }
                ]
            });
        msgWindow.show({
            ui: 'notification-error',
            title: 'Error during removing',
            msg: 'The communication task could not be removed because of an error in the database.',
            icon: Ext.MessageBox.ERROR
        });
    },

    deleteTask: function (id) {
        var self = this,
            tasksView = self.getTasksView(),
            grid = tasksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/cts/comtasks/' + id,
            confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        xtype: 'button',
                        text: 'Remove',
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
                                success: function () {
                                    confirmMessage.close();
                                    self.getRulesGridPagingToolbarTop().totalCount = 0;
                                    Ext.create('widget.uxNotification', {
                                        html: 'Successfully removed',
                                        ui: 'notification-success'
                                    }).show();
                                    self.store.loadPage(1, {
                                        callback: function () {
                                            grid.getSelectionModel().select(0);
                                            grid.fireEvent('itemclick', grid.getView(), grid.getSelectionModel().getLastSelected());
                                        }
                                    });
                                },
                                failure: function (response) {
                                    confirmMessage.close();
                                    self.showDatabaseError();
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
            msg: '<p>This communication task will no longer be available.</p>',
            icon: Ext.MessageBox.WARNING
        });
    }
});
