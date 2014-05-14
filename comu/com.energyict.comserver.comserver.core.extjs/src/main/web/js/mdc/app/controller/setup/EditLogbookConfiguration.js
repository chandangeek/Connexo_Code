Ext.define('Mdc.controller.setup.EditLogbookConfiguration', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookConfigurations'
    ],

    views: [
        'setup.deviceconfiguration.EditLogbookConfiguration'
    ],

    refs: [
        {
            ref: 'editLogbookConfiguration',
            selector: 'edit-logbook-configuration'
        }
    ],

    init: function () {
        this.control({
            'edit-logbook-configuration button[action=save]': {
                click: this.saveLogbookType
            }
        });
    },

    showDatabaseError: function (msges) {
        var self = this,
            editView = self.getEditLogbookConfiguration();
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
                        window.location = '#setup/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations';
                    }
                }
            ],
            listeners: {
                close: {
                    fn: function () {
                        editView.enable();
                    }
                }
            }
        });
        editView.disable();
    },

    saveLogbookType: function (btn) {
        var self = this,
            editView = self.getEditLogbookConfiguration(),
            form = editView.down('form').getForm(),
            formErrorsPanel = Ext.ComponentQuery.query('edit-logbook-configuration panel[name=errors]')[0],
            jsonValues = Ext.JSON.encode(form.getValues()),
            url = '/api/dtc/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations/' + editView.logbookConfigurationId,
            header = {
                style: 'msgHeaderStyle'
            },
            bodyItem = {},
            msges = [],
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: editView
            });
        if (form.isValid()) {
            formErrorsPanel.hide();
            preloader.show();
            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: jsonValues,
                success: function () {
                    window.location.href = '#setup/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations';
                    header.text = 'Successfully edited';
                    self.getApplication().fireEvent('isushowmsg', {
                        type: 'notify',
                        msgBody: [header],
                        y: 10,
                        showTime: 5000
                    });
                },
                failure: function (response) {
                    var result = Ext.decode(response.responseText);
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
                                        window.location = '#setup/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations/' + editView.logbookConfigurationId + '/edit';
                                    }
                                }
                            ],
                            listeners: {
                                close: {
                                    fn: function () {
                                        editView.enable();
                                    }
                                }
                            }
                        });
                        editView.disable();
                    }
                    else {
                        header.text = 'Error during editing';
                        msges.push(header);
                        bodyItem.style = 'msgItemStyle';
                        bodyItem.text = 'The logbook configuration could not be edited because of an error in the database.';
                        msges.push(bodyItem);
                        self.showDatabaseError(msges);
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        } else {
            formErrorsPanel.hide();
            formErrorsPanel.removeAll();
            formErrorsPanel.add({
                html: 'There are errors on this page that require your attention.'
            });
            formErrorsPanel.show();
        }
    }
});


