Ext.define('Mdc.controller.setup.LogbookTypesOverview', {
    extend: 'Ext.app.Controller',
    stores: [ 'Mdc.store.Logbook' ],
    views: [
        'setup.logbooktype.LogbookTypeSetup',
        'setup.logbooktype.LogbookTypeGrid',
        'setup.logbooktype.LogbookTypePreview',
        'setup.logbooktype.LogbookTypeActionMenu',
        'setup.logbooktype.LogbookTypeCreateUpdateForm'
    ],
    refs: [
        { ref: 'logbookTypeSetupPanel', selector: 'logbookTypeSetup' },
        { ref: 'formPanel', selector: 'logbookTypeCreateUpdateForm' }
    ],
    init: function () {
        this.control({
            'logbookTypeSetup logbookTypeGrid': {
                afterrender: this.loadStore,
                itemclick: this.loadGridItemDetail
            },
            'logbookTypeSetup logbookTypeGrid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'logbookTypeSetup logbookTypePreview logbookTypeActionMenu': {
                click: this.chooseAction
            },
            'logbookTypeCreateUpdateForm button[action=create]': {
                click: this.onSubmit
            },

            'logbookTypeCreateUpdateForm button[action=edit]': {
                click: this.onSubmit
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
    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('logbookTypePreview')[0],
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
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
        itemPanel.down('logbookTypeActionMenu').record = record;
        preloader.destroy();
    },
    checkLogBookTypesCount: function () {
        var numberOfLogbooksContainer = Ext.ComponentQuery.query('container[name=logbookTypeRangeCounter]')[0],
            grid = Ext.ComponentQuery.query('logbookTypeGrid')[0];
        if (grid) {
            var gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                widget = Ext.widget('container', {
                    html: this.store.getCount() + ' ' + Uni.I18n.translatePlural('logbooktype.logbookTypes', this.store.getCount(), 'MDC', 'logbook type(s)')
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
//    showOverview: function () {
//        var widget = Ext.widget('logbookTypeSetup');
//        this.getApplication().fireEvent('changecontentevent', widget);
//    },
    chooseAction: function (menu, item) {
        var action = item.action;
        switch (action) {
            case 'editLogbookType':
                window.location.href = '#/administration/logbooktypes/edit/' + menu.record.getId();
                break;
            case 'removeLogbookType':
                this.removeLogbook(menu.record.getId());
                break;
        }
    },
    removeLogbook: function (logBookTypeId) {
        var self = this,
            overview = Ext.ComponentQuery.query('logbookTypeSetup')[0],
            record = overview.down('form').getRecord();

        Ext.create('Uni.view.window.Confirmation').show({
            title: Ext.String.format(Uni.I18n.translate('logbooktype.remove.confirmation.title', 'MDC', 'Remove {0}?'), record.data.name),
            msg: Uni.I18n.translate('logbooktype.remove.confirmation.msg', 'MDC', 'This logbook type will no longer be available'),
            config: {
                self: self,
                record: record,
                logBookTypeId: logBookTypeId
            },
            fn: self.processRemovingLogbookType
        });
    },
    processRemovingLogbookType: function (state, text, cfg) {
        if (state === 'confirm') {
            var self = cfg.config.self,
                logBookStore = self.store,
                preloader = Ext.create('Ext.LoadMask', {
                    msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                    target: self.getLogbookTypeSetupPanel()
                });
            preloader.show();
            Ext.Ajax.request({
                url: '/api/mds/logbooktypes/' + cfg.config.logBookTypeId,
                method: 'DELETE',
                success: function () {
                    Ext.create('widget.uxNotification', {
                        html: Uni.I18n.translate('logbooktype.remove.successfull', 'MDC', 'Successfully removed'),
                        ui: 'notification-success'
                    }).show();
                    logBookStore.load();
                },
                failure: function (response) {
                    if (response.status == 400) {
                        var record = cfg.config.record,
                            result = Ext.decode(response.responseText, true),
                            title = Ext.String.format(Uni.I18n.translate('logbooktype.remove.failed', 'MDC', 'Failed to remove {0}'), record.data.name),
                            message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');

                        if(!Ext.isEmpty(response.statusText)) {
                            message = response.statusText;
                        }
                        if(result && result.error) {
                            message = result.error;
                        }
                        self.getApplication().getController('Uni.controller.Error').showError(title, message);
                    }

                },
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    },
    showOverview: function (id) {
        var self = this,
            widget = Ext.widget('logbookTypeCreateUpdateForm'),
            form = widget.down('form'),
            btn = form.down('#submit'),
            title;

        this.getApplication().fireEvent('changecontentevent', widget);

        if (id) {
            this.crumbId = this.logId = id;
            self.getModel('Mdc.model.Logbook').load(id, {
                success: function (record) {
                    form.loadRecord(record);
                    self.logbookAssigned(record);
                }
            });
            title = 'Edit logbook type';
            btn.setText('Save');
            btn.action = 'edit';
        } else {
            title = 'Create logbook type';
            btn.setText('Add');
            btn.action = 'create';
            self.getFormPanel().down('#logbookTypeObis').setDisabled(false);
        }

        form.setTitle(title);
    },

    logbookAssigned: function (record) {
        if (!record.raw.isInUse) {
            this.getFormPanel().down('#logbookTypeObis').setDisabled(false);
        }
    },

    createRequest: function (form, formErrorsPanel, preloader) {
        var self = this,
            jsonValues = Ext.JSON.encode(form.getValues());

        Ext.Ajax.request({
            url: '/api/mds/logbooktypes',
            method: 'POST',
            jsonData: jsonValues,
            success: function () {
                window.location.href = '#/administration/logbooktypes';

                Ext.create('widget.uxNotification', {
                    html: 'Successfully created',
                    ui: 'notification-success'
                }).show();
            },
            failure: function (response) {
                var result = Ext.decode(response.responseText, true);
                if (result !== null) {
                    Ext.widget('messagebox').show({
                        ui: 'notification-error',
                        title: result.error,
                        msg: result.message,
                        icon: Ext.MessageBox.ERROR,
                        buttons: Ext.MessageBox.CANCEL
                    });
                } else {
                    Ext.widget('messagebox').show({
                        ui: 'notification-error',
                        title: 'Error during creation.',
                        msg: 'The logbook type could not be created. There was a problem accessing the database',
                        icon: Ext.MessageBox.ERROR,
                        buttons: Ext.MessageBox.CANCEL
                    });
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    editRequest: function (formPanel, form, formErrorsPanel, preloader) {
        var self = this,
            record = form.getRecord(),
            jsonValues = Ext.JSON.encode(form.getValues());
        Ext.Ajax.request({
            url: '/api/mds/logbooktypes/' + self.logId,
            method: 'PUT',
            jsonData: jsonValues,
            waitMsg: 'Loading...',
            success: function () {
                window.location.href = '#/administration/logbooktypes';

                Ext.create('widget.uxNotification', {
                    html: 'Successfully edited',
                    ui: 'notification-success'
                }).show();
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
                            msg: 'Logbook type could not be deleted. There was a problem accessing the database',
                            icon: Ext.MessageBox.ERROR
                        })
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showErrorsPanel: function (formErrorsPanel) {
        formErrorsPanel.hide();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: 'There are errors on this page that require your attention.'
        });
        formErrorsPanel.show();
    },

    onSubmit: function (btn) {
        var self = this,
            formPanel = self.getFormPanel(),
            form = formPanel.down('form').getForm(),
            formErrorsPanel = Ext.ComponentQuery.query('logbookTypeCreateUpdateForm #errors')[0],
            preloader;
        if (form.isValid()) {
            formErrorsPanel.hide();
            self.trimFields();
            switch (btn.action) {
                case 'create':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Creating logbook type",
                        name: 'log-form-create',
                        target: formPanel
                    });
                    preloader.show();
                    self.createRequest(form, formErrorsPanel, preloader);
                    break;
                case 'edit':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Editing logbook type",
                        name: 'log-form-edit',
                        target: formPanel
                    });
                    preloader.show();
                    self.editRequest(formPanel, form, formErrorsPanel, preloader);
                    break;
            }
        } else {
            self.showErrorsPanel(formErrorsPanel);
        }
    },

    trimFields: function () {
        var nameField = Ext.ComponentQuery.query('logbookTypeCreateUpdateForm #logbookTypeName')[0],
            nameValue = Ext.util.Format.trim(nameField.value);
        nameField.setValue(nameValue);
    }
});