/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.controller.UserSecuritySettings', {
    extend: 'Ext.app.Controller',
    stores: [
        'Usr.store.UserSecuritySettings',
    ],
    views: [
        'Usr.view.userSecuritySettings.Setup'
    ],

    init: function () {
        this.control({
            'userSecuritySettings button[action=save]': {
                click: this.updateUserSecuritySettings
            }
        });
    },
    showOverview: function () {
        var me = this,
            widget = Ext.widget('userSecuritySettings'),
            store = Ext.create('Usr.store.UserSecuritySettings');

        store.load({
            callback: function (records, operation, success) {
                if (success && records.length > 0) {
                    var detailsForm = widget.down('#user-security-settings-form');
                    me.getApplication().fireEvent('changecontentevent', widget);
                    detailsForm.loadRecord(records[0]);
                }
            }
        });

    },

    updateUserSecuritySettings: function (button) {
        var me = this,
            form = button.up('form'),
            record = form.getRecord(),
            formErrorsPanel = form.down('[name=form-errors]');

        form.updateRecord(record);

        record.save({
            backUrl: '#/administration',
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('userSecuritySettings.saved', 'USR', 'User security settings saved'));
                location.href = '#/administration';
            },
            failure: function (record, operation) {
                if (operation.response.status === 400) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        if (json.errors.length > 0) {
                            formErrorsPanel.show();
                        }
                    }
                }
            }
        });
    }

});