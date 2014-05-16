Ext.define('Mdc.controller.setup.LogbookTypesOverview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Mdc.store.Logbook'
    ],

    views: [
        'Ext.ux.window.Notification',
        'setup.logbooktype.LogbookTypesOverview',
        'setup.logbooktype.List',
        'setup.logbooktype.Item',
        'setup.logbooktype.ActionMenu',
        'setup.logbooktype.DockedButtons',
        'setup.logbooktype.EmptyListMessage',
        'setup.logbooktype.FloatingPanel'
    ],

    init: function () {
        this.control({
            'logbook-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'logbook-overview logbook-list': {
                afterrender: this.loadStore,
                itemclick: this.loadGridItemDetail
            },
            'logbook-overview logbook-list actioncolumn': {
                click: this.showItemAction
            },
            'logbook-action-menu': {
                click: this.chooseAction
            },
            'logbook-floating-panel button[action=cancel]': {
                click: this.closeFloatingMessage
            },
            'logbook-floating-panel button[action=delete]': {
                click: this.deleteLogbook
            },
            'logbook-floating-panel': {
                beforeclose: this.enableOverviewPanel
            },
            'uxNotification': {
                beforeclose: this.enableOverviewPanel
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

    loadStore: function() {
        this.store.load();
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this;
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: '#/administration'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Logbook types',
                href: 'logbooktypes'
            });
        breadcrumbParent.setChild(breadcrumbChild1);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    checkLogBookTypesCount: function () {
        var numberOfLogbooksContainer = Ext.ComponentQuery.query('logbook-overview logbook-docked-buttons container[name=LogBookCount]')[0],
            grid = Ext.ComponentQuery.query('logbook-overview logbook-list')[0],
            gridView = grid.getView(),
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
        preloader.destroy();
    },

    showOverview: function () {
        var widget = Ext.widget('logbook-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
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
                xtype: 'logbook-action-menu',
                logBookId: record.getData().id,
                isDefault: record.getData().default
            }
        });
        this.gridActionBtn.showMenu();
    },

    /**
     * Hide menu for actioncolumn icon.
     */
    hideItemAction: function () {
        if (this.gridActionBtn) {
            this.gridActionBtn.destroy();
        }

        if (this.gridActionIcon) {
            this.gridActionIcon.show();
            this.gridActionIcon.setHeight(22);
        }
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        switch (action) {
            case 'edit':
                window.location.href = '#/administration/logbooktypes/edit/' + menu.logBookId;
                break;
            case 'delete':
                this.showConfirmationPanel(menu.logBookId);
                break;
        }
    },

    deleteLogbook: function (btn) {
        btn.up('logbook-floating-panel').hide();
        if (!Ext.isEmpty(this.lastLogBookIdToRemove)) {
            var logBookId = this.lastLogBookIdToRemove,
                cssErrorClass = 'logbook-delete-error',
                cssSuccessClass = 'logbook-delete-success',
                overview = Ext.ComponentQuery.query('logbook-overview')[0],
                logBookStore = this.store;

            this.enableOverviewPanel();
            Ext.Ajax.request({
                url: '/api/mds/logbooktypes/'+ logBookId,
                method: 'DELETE',
                waitMsg: 'Deleting...',
                success: function () {
                    successMessage = Ext.widget('logbook-floating-panel', {
                        width: overview.getWidth() / 3,
                        title: "Success",
                        html: "<br> Logbook has been removed successfully <br><br>",
                        autoCloseDelay: 2000,
                        cls: cssSuccessClass
                    });
                    successMessage.getDockedItems()[0].hide();
                    successMessage.show();
                    logBookStore.load();
                },
                failure: function (result, request ) {
                    var jsonData = Ext.JSON.decode(result.responseText);
                    var errorMessage = Ext.widget('logbook-floating-panel', {
                        width: overview.getWidth() - 20,
                        title: "Error during deletion logbook type",
                        html: "<br>" + jsonData.message + "<br><br>",
                        cls: cssErrorClass
                    });
                    errorMessage.getDockedItems()[0].addCls(cssErrorClass);
                    errorMessage.down('button[name=cancel]').hide();
                    errorMessage.down('button[name=retry]').hide();
                    errorMessage.down('button[name=delete]').hide();
                    errorMessage.show();
                }
            });
        }
    },

    enableOverviewPanel: function () {
        var overviewPanel = Ext.ComponentQuery.query('logbook-overview')[0];
        if (!Ext.isEmpty(overviewPanel)) {
            overviewPanel.enable();
        }
    },

    closeFloatingMessage: function (btn) {
        this.enableOverviewPanel();
        btn.up('logbook-floating-panel').hide();
    },

    showConfirmationPanel: function (logBookId) {
        this.lastLogBookIdToRemove = logBookId;
        var overview = Ext.ComponentQuery.query('logbook-overview')[0],
            cssConfirmationClass = 'logbook-confirmation-message',
            confirmationMessage = Ext.widget('logbook-floating-panel', {
                width: overview.getWidth() / 3,
                title: "Delete logbook?",
                html: "<br>This logbook type will no longer be available<br><br>",
                cls: cssConfirmationClass
            });
        confirmationMessage.getDockedItems()[0].addCls(cssConfirmationClass);
        confirmationMessage.down('button[name=close]').hide();
        confirmationMessage.down('button[name=retry]').hide();
        overview.disable();
        confirmationMessage.show();
    }

});