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
                        window.location = '#/administration/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations';
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
            record = form.getRecord(),
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
                    window.location.href = '#/administration/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations';
                    Ext.create('widget.uxNotification', {
                        html: 'Successfully updated',
                        ui: 'notification-success'
                    }).show();
                },
                failure: function (response) {
                    preloader.destroy();
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
                            title: 'Failed to update ' + record.data.name,
                            msg: 'Logbook configuration could not be updated. There was a problem accessing the database',
                            icon: Ext.MessageBox.ERROR
                        })
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


