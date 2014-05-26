Ext.define('Isu.controller.CommunicationTasksView', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CommunicationTasks'
    ],

    views: [
        'Isu.view.administration.communicationtasks.View',
        'Isu.view.administration.communicationtasks.Form',
        'Isu.view.administration.communicationtasks.List'
    ],

    mixins: [
        'Isu.util.IsuGrid'
    ],

    refs: [
        {
            ref: 'tasksView',
            selector: 'communication-tasks-view'
        },
        {
            ref: 'itemPanel',
            selector: 'communication-tasks-item'
        },
        {
            ref: 'tasksGrid',
            selector: 'communication-tasks-list'
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
            selector: 'communication-tasks-list pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'communication-tasks-view breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'communication-tasks-view communication-tasks-list gridview': {
                itemclick: this.showTaskDetails,
                refresh: this.onCommunicationTasksGridRefresh
            },
            'communication-tasks-view communication-tasks-list actioncolumn': {
                click: this.showItemAction
            },
            'communication-tasks-action-menu': {
                beforehide: this.hideItemAction,
                click: this.chooseCommunicationTasksAction
            }
        });

        this.actionMenuXtype = 'communication-tasks-action-menu';
        this.gridItemModel = this.getModel('Isu.model.CommunicationTasks');
        this.store = this.getStore('Isu.store.CommunicationTasks');
    },

    showOverview: function () {
        var widget = Ext.widget('communication-tasks-view');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this,
            breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Communication tasks',
                href: 'communicationtasks'
            });
        breadcrumbParent.setChild(breadcrumbChild1);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    chooseCommunicationTasksAction: function (menu, item) {
        var self = this,
            action = item.action;

        switch (action) {
            case 'edit':
                window.location.href = '#/issue-administration/communicationtasks/' + self.taskId;
                break;
            case 'delete':
                this.deleteTask(self.taskId);
                break;
        }
    },

    onCommunicationTasksGridRefresh: function (grid) {
        var tasksGrid = this.getTasksGrid(),
            itemPanel = this.getItemPanel(),
            selectionModel = tasksGrid.getView().getSelectionModel();
        if (this.store.getTotalCount() < 1) {
            tasksGrid.hide();
            itemPanel.hide();
            this.getEmptyPanel().show();
        } else {
            tasksGrid.getView().getSelectionModel().select(0);
            this.showTaskDetails(tasksGrid.getView(), selectionModel.getLastSelected());
        }
    },


    showTaskDetails: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('communication-tasks-form'),
            nameField = this.getNameField(),
            commandsField = this.getCommandsField(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.clearHighlight();
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
                    str += command.action + ' ' + command.category + '<br/>';
                });
                commandsField.setValue(str);
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showDatabaseError: function (msges) {
        var self = this,
            tasksView = self.getTasksView();
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
                        window.location = '#/issue-administration/communicationtasks';
                    }
                }
            ],
            listeners: {
                close: {
                    fn: function () {
                        tasksView.enable();
                    }
                }
            }
        });
        tasksView.disable();
    },

    deleteTask: function (id) {
        var self = this,
            tasksView = self.getTasksView(),
            grid = tasksView.down('grid'),
            header = {
                style: 'msgHeaderStyle'
            },
            bodyItem = {},
            msges = [],
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/cts/comtasks/' + id,
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
                                    self.getRulesGridPagingToolbarTop().totalCount = 0;
                                    header.text = 'Successfully removed';
                                    self.getApplication().fireEvent('isushowmsg', {
                                        type: 'notify',
                                        msgBody: [header],
                                        y: 10,
                                        showTime: 5000
                                    });
                                    self.store.loadPage(1, {
                                        callback: function () {
                                            grid.getSelectionModel().select(0);
                                            grid.fireEvent('itemclick', grid.getView(), grid.getSelectionModel().getLastSelected());
                                        }
                                    });
                                },
                                failure: function (response) {
                                    confirmMessage.close();
                                    header.text = 'Error during removing';
                                    msges.push(header);
                                    bodyItem.style = 'msgItemStyle';
                                    bodyItem.text = "The communication task could not be removed because it's in use.";
                                    msges.push(bodyItem);
                                    self.showDatabaseError(msges);
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
            title: 'Remove ' + record.data.name + ' ?',
            msg: '<p>This communication task will no longer be available.</p>',
            icon: Ext.MessageBox.QUESTION,
            cls: 'isu-delete-message'
        });
    }
});