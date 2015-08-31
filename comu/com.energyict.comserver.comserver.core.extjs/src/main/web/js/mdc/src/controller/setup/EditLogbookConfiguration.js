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
                    text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                    cls: 'isu-btn-link',
                    hnd: function () {
                        window.location = '#/administration/devicetypes/' + encodeURIComponent(editView.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(editView.deviceConfigurationId) + '/logbookconfigurations';
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
            formErrorsPanel = editView.down('#form-errors'),
            record = form.getRecord(),
            jsonValues = Ext.JSON.encode(form.getValues()),
            url = '/api/dtc/devicetypes/' + editView.deviceTypeId + '/deviceconfigurations/' + editView.deviceConfigurationId + '/logbookconfigurations/' + editView.logbookConfigurationId,
            header = {
                style: 'msgHeaderStyle'
            },
            bodyItem = {},
            msges = [],
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading','MDC','Loading...'),
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
                    window.location.href = '#/administration/devicetypes/' + encodeURIComponent(editView.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(editView.deviceConfigurationId) + '/logbookconfigurations';
                    self.getApplication().fireEvent('acknowledge', 'Logbook configuration saved');
                },
                failure: function (response) {
                    if(response.status == 400) {
                        var result = Ext.decode(response.responseText, true),
                            errorTitle = Ext.String.format(Uni.I18n.translate('logbookconfiguration.failedToUpdate','','Failed to update {0}'),record.data.name),
                            errorText = Uni.I18n.translate('logbookconfiguration.configurationCouldNotBeUpdated','MDC','Logbook configuration could not be updated. There was a problem accessing the database');

                        if (result !== null) {
                            errorTitle = result.error;
                            errorText = result.message;
                        }

                        self.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        } else {
            formErrorsPanel.show();
        }
    }
});


