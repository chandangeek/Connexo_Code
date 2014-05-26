Ext.define('Mdc.controller.setup.LogForm', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
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
            'form-logbook breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },

            'form-logbook button[action=create]': {
                click: this.onSubmit
            },

            'form-logbook button[action=edit]': {
                click: this.onSubmit
            }
        });
    },

    showOverview: function (id) {
        var self = this;
        var widget = Ext.widget('form-logbook');
        var form = widget.down('form');
        var btn = form.down('button[name=logAction]');
        var title;

        this.getApplication().getController('Mdc.controller.Main').showContent(widget);

        if (id) {
            this.crumbId = this.logId = id;
            self.getModel('Mdc.model.Logbook').load(id, {
                success: function (record) {
                    form.loadRecord(record);
                }
            });
            title = 'Edit logbook type';
            btn.setText('Save');
            btn.action = 'edit';
        } else {
            title = 'Create logbook type';
            btn.setText('Add');
            btn.action = 'create';
        }

        form.setTitle(title);
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
            }),
            breadcrumbChild2;
        if (me.crumbId) {
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Edit logbook type',
                href: 'edit'
            });
            delete me.crumbId;
        } else {
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Create logbook type',
                href: 'create'
            });
        }
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    createRequest: function(form, formErrorsPanel, preloader) {
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
                        msg: 'The logbook type could not be created because of an error in the database.',
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

    editRequest: function(formPanel, form, formErrorsPanel, preloader) {
        var self = this,
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
                        title: 'Error during edit.',
                        msg: 'The logbook type could not be created because of an error in the database.',
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

    showErrorsPanel: function(formErrorsPanel) {
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




