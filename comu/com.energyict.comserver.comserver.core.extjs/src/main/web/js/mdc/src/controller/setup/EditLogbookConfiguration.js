/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
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
        var me = this,
            editView = me.getEditLogbookConfiguration(),
            form = editView.down('form'),
            formErrorsPanel = editView.down('#form-errors'),
            record = form.getRecord(),
            page = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            backUrl = me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/logbookconfigurations').buildUrl();

        formErrorsPanel.hide();
        formErrorsPanel.hide();
        form.updateRecord(record);
        page.setLoading();
        record.save({
            backUrl: backUrl,
            success: function () {
                window.location.href = backUrl;
                me.getApplication().fireEvent('acknowledge', 'Logbook configuration saved');
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var result = Ext.decode(operation.response.responseText, true);
                    if (result && result.errors) {
                        Ext.Array.each(result.errors, function (error) {
                            if (error.id === 'overruledObisCode.obisCode') {
                                form.down('#obis-code-container').setActiveError(error.msg);
                            }
                        });
                        formErrorsPanel.show();
                    }
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});