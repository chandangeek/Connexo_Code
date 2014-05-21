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

        this.getApplication().fireEvent('changecontentevent', widget);

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

    showNameNotUniqueError: function(result, msges, formErrorsPanel) {
        var self = this,
            formPanel = self.getFormPanel(),
            nameField = formPanel.down('form').down('[name=name]'),
            formButton = formPanel.down('form').down('button'),
            formCancelButton = formPanel.down('form').down('button[name=cancel]');
        Ext.Array.each(result.errors, function (item) {
            var bodyItem = {};
            bodyItem.style = 'msgItemStyle';
            bodyItem.text = item.msg;
            nameField.markInvalid(item.msg);
            msges.push(bodyItem);
        });
        if (!nameField.errorEl) {
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
                            window.location = '#/administration/logbooktypes';
                        }
                    }
                ],
                listeners: {
                    close: {
                        fn: function () {
                            formButton.enable();
                            formCancelButton.enable();
                        }
                    }
                }
            });
            formButton.disable();
            formCancelButton.disable();
        } else {
            self.showErrorsPanel(formErrorsPanel);
        }
    },

    showDatabaseError: function(msges) {
        var self = this,
            formPanel = self.getFormPanel();
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
                        window.location = '#/administration/logbooktypes';
                    }
                }
            ],
            listeners: {
                close: {
                    fn: function () {
                        formPanel.enable();
                    }
                }
            }
        });
        formPanel.disable();
    },

    createRequest: function(form, formErrorsPanel, preloader) {
        var self = this,
            header = {
                style: 'msgHeaderStyle'
            },
            msges = [],
            jsonValues = Ext.JSON.encode(form.getValues());
        Ext.Ajax.request({
            url: '/api/mds/logbooktypes',
            method: 'POST',
            jsonData: jsonValues,
            success: function () {
                window.location.href = '#/administration/logbooktypes';
                header.text = 'Successfully created';
                self.getApplication().fireEvent('isushowmsg', {
                    type: 'notify',
                    msgBody: [header],
                    y: 10,
                    showTime: 5000
                });
            },
            failure: function (response) {
                header.text = 'Error during creation.';
                msges.push(header);
                var result = Ext.decode(response.responseText, true);
                if (result !== null) {
                    self.showNameNotUniqueError(result, msges, formErrorsPanel);
                } else {
                    var bodyItem = {};
                    bodyItem.style = 'msgItemStyle';
                    bodyItem.text = 'The logbook type could not be created because of an error in the database.';
                    msges.push(bodyItem);
                    self.showDatabaseError(msges);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    editRequest: function(formPanel, form, formErrorsPanel, preloader) {
        var self = this,
            header = {
                style: 'msgHeaderStyle'
            },
            msges = [],
            jsonValues = Ext.JSON.encode(form.getValues());
        Ext.Ajax.request({
            url: '/api/mds/logbooktypes/' + self.logId,
            method: 'PUT',
            jsonData: jsonValues,
            waitMsg: 'Loading...',
            success: function () {
                window.location.href = '#/administration/logbooktypes';
                header.text = 'Successfully edited';
                self.getApplication().fireEvent('isushowmsg', {
                    type: 'notify',
                    msgBody: [header],
                    y: 10,
                    showTime: 5000
                });
            },
            failure: function (response) {
                header.text = 'Error during edit';
                msges.push(header);
                var result = Ext.decode(response.responseText, true);
                if (result !== null) {
                    self.showNameNotUniqueError(result, msges, formErrorsPanel);
                } else {
                    var bodyItem = {};
                    bodyItem.style = 'msgItemStyle';
                    bodyItem.text = 'The logbook type could not be edited because of an error in the database.';
                    msges.push(bodyItem);
                    self.showDatabaseError(msges);
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




