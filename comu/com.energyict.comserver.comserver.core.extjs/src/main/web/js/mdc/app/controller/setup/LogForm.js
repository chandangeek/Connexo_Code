Ext.define('Mdc.controller.setup.LogForm', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'setup.logbooktype.LogForm'
    ],

    stores: [
        'Mdc.store.Logbook'
    ],

    refs: [
        {
            ref: 'formPanel',
            selector: 'form-logbook'
        },
        {
            ref: 'logBtn',
            selector: 'form-logbook button[name=logAction]'
        },
        {
            ref: 'logTitle',
            selector: 'form-logbook [name=header]'
        }
    ],

    init: function () {
        this.control({
            'form-logbook button[action=create]': {
                click: this.onSubmit
            },

            'form-logbook button[action=edit]': {
                click: this.onSubmit
            }
        });
    },

    showOverview: function (id) {
        var self = this,
            widget = Ext.widget('form-logbook'),
            form = widget.down('form'),
            obisField = form.down('#obis'),
            btn = form.down('button[name=logAction]'),
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
            self.getFormPanel().down('#obis').setDisabled(false);
        }

        form.setTitle(title);
    },

    logbookAssigned: function (record) {
        if (!record.raw.isInUse) {
            this.getFormPanel().down('#obis').setDisabled(false);
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
            formErrorsPanel = Ext.ComponentQuery.query('form-logbook panel[name=errors]')[0],
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
        var nameField = Ext.ComponentQuery.query('form-logbook textfield[name=name]')[0],
            nameValue = Ext.util.Format.trim(nameField.value);
        nameField.setValue(nameValue);
    }
});




