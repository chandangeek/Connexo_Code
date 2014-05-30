Ext.define('Isu.controller.NotifySend', {
    extend: 'Ext.app.Controller',
    actionId: null,
    views: [
        'workspace.issues.NotifySend'
    ],

    stores: [
        'Actions'
    ],

    refs: [
        {
            ref: 'notifyView',
            selector: 'notify-user'
        }
    ],

    init: function () {
        this.control({
            'notify-user button[action=notifySend]': {
                click: this.submit
            }
        });
    },

    showOverview: function () {
        var self = this,
            widget = Ext.widget('notify-user'),
            view = self.getNotifyView();
        self.getApplication().fireEvent('changecontentevent', widget);
        self.getStore('Actions').load({
            callback: function (records) {
                var str = window.location.href;
                if (str.match(/notify/) !== null) {
                    view.down('#notifyPanel').setTitle('Notify user');
                    view.down('#notifySend').setText('Notify');
                    self.notifyUser(records, view);
                } else {
                    view.down('#notifyPanel').setTitle('Send to inspect');
                    view.down('#notifySend').setText('Send to inspect');
                    self.sendSomeone(records, view);
                }
            }
        })
    },

    notifyUser: function (records, view) {
        var self = this;
        Ext.Array.each(records, function (record) {
            if (record.data.parameters.recepients && record.data.parameters.recepients.control.alias === 'emailList') {
                Ext.apply(Ext.form.VTypes, {
                    emailVtype: function (val) {
                        var email = /^((([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z?]{2,5}){1,25})*(\n?)*)*$/;
                        return email.test(val);
                    },
                    emailVtypeText: 'This field should contains one e-mail address per line'
                });
                self.id = record.data.id;
                view.down('form').add({
                    xtype: 'textarea',
                    itemId: 'emailList',
                    name: record.data.parameters.recepients.key,
                    width: 500,
                    height: 150,
                    allowBlank: record.data.parameters.recepients.constraint.optional,
                    labelSeparator: ' *',
                    fieldLabel: record.data.parameters.recepients.label,
                    vtype: 'emailVtype',
                    emptyText: 'user@example.com',
                    maxLength: record.data.parameters.recepients.constraint.max
                })
            }
            if (record.data.parameters.emailBody && record.data.parameters.emailBody.control.alias === 'textArea') {
                view.down('form').add({
                    xtype: 'textarea',
                    itemId: 'emailBody',
                    name: record.data.parameters.emailBody.key,
                    width: 500,
                    height: 150,
                    allowBlank: record.data.parameters.emailBody.constraint.optional,
                    labelSeparator: ' *',
                    fieldLabel: record.data.parameters.emailBody.label,
                    msgTarget: 'under',
                    maxLength: record.data.parameters.emailBody.constraint.max
                })
            }
        })
    },

    sendSomeone: function (records, view) {
        var self = this;
        Ext.Array.each(records, function (record) {
            if (record.data.parameters.user && record.data.parameters.user.control.alias === 'userCombobox') {
                self.id = record.data.id;
                view.down('form').add({
                    xtype: 'issues-assignee-combo',
                    itemId: 'assignee',
                    name: record.data.parameters.user.key,
                    fieldLabel: record.data.parameters.user.label,
                    forceSelection: true,
                    anyMatch: true,
                    labelSeparator: ' *',
                    emptyText: 'select an assignee',
                    tooltipText: 'Start typing for assignee'
                })
            }
        });
    },

    trimFields: function () {
        var self = this,
            emailListField = self.getNotifyView().down('#emailList'),
            emailBodyField = self.getNotifyView().down('#emailBody'),
            emailListTrim,
            emailBodyTrim;
        if (!Ext.isEmpty(emailListField.value)) {
            emailListTrim = Ext.util.Format.trim(emailListField.value);
            emailListField.setValue(emailListTrim);
        }
        if (!Ext.isEmpty(emailBodyField.value)) {
            emailBodyTrim = Ext.util.Format.trim(emailBodyField.value);
            emailBodyField.setValue(emailBodyTrim);
        }
    },

    submit: function () {
        var self = this,
            notifyView = self.getNotifyView(),
            form = notifyView.down('form').getForm(),
            sendingData = {},
            preloader,
            str = window.location.href,
            formErrorsPanel = Ext.ComponentQuery.query('notify-user panel[name=errors]')[0];
        if (str.match(/notify/) !== null) {
            self.trimFields();
            if (form.isValid()) {
                formErrorsPanel.hide();
                sendingData.id = self.id;
                sendingData.parameters = form.getValues();
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Notifying user",
                    target: notifyView
                });
                self.sendData(sendingData, preloader);
            } else {
                self.showErrorsPanel();
            }
        } else {
            if (notifyView.down('#assignee').value !== null && form.isValid()) {
                notifyView.down('#assignee').clearInvalid();
                formErrorsPanel.hide();
                sendingData.id = self.id;
                sendingData.parameters = form.getValues();
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Sending data",
                    target: notifyView
                });
                self.sendData(sendingData, preloader);
            } else {
                self.showErrorsPanel();
                notifyView.down('#assignee').markInvalid('This is a required field');
            }
        }
    },

    sendData: function(sendingData, preloader) {
        Ext.Ajax.request({
            url: '/api/isu/issue/' + window.location.hash.match(/[0-9]/)[0] + '/action',
            method: 'PUT',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/workspace/datacollection/issues';
                Ext.create('widget.uxNotification', {
                    html: 'Operation completed successfully!',
                    ui: 'notification-success'
                }).show();
            },
            failure: function () {
                Ext.create('widget.uxNotification', {
                    html: 'Operation Failed!',
                    ui: 'notification-error'
                }).show();
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showErrorsPanel: function() {
        var formErrorsPanel = Ext.ComponentQuery.query('notify-user panel[name=errors]')[0];
        formErrorsPanel.hide();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: 'There are errors on this page that require your attention.'
        });
        formErrorsPanel.show();
    }
});



