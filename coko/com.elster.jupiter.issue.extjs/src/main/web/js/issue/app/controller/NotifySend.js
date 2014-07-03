Ext.define('Isu.controller.NotifySend', {
    extend: 'Ext.app.Controller',
    actionId: null,
    issId: null,
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

    mixins: [
        'Isu.util.CreatingControl'
    ],

    init: function () {
        this.control({
            'notify-user button[action=notifySend]': {
                click: this.submit
            }
        });
    },

    showNotifySend: function (id) {
        var self = this,
            widget = Ext.widget('notify-user'),
            view = self.getNotifyView();
        self.issId = id;
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
        var self = this,
            control;
        Ext.Array.each(records, function (record) {
            if (record.data.parameters.recepients && record.data.parameters.recepients.control.xtype === 'emailList') {
                control = self.createControl(record.data.parameters.recepients);
                self.actionId = record.data.id;
                view.down('form').add(control);
            }
            if (record.data.parameters.emailBody && record.data.parameters.emailBody.control.xtype === 'textArea') {
                control = self.createControl(record.data.parameters.emailBody);
                view.down('form').add(control);
            }
        })
    },

    sendSomeone: function (records, view) {
        var self = this,
            control;
        Ext.Array.each(records, function (record) {
            if (record.data.parameters.user && record.data.parameters.user.control.xtype === 'userCombobox') {
                self.actionId = record.data.id;
                control = self.createControl(record.data.parameters.user);
                view.down('form').add(control);
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
                sendingData.id = self.actionId;
                sendingData.parameters = form.getValues();
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Notifying user",
                    target: notifyView
                });
                preloader.show();
                self.sendData(sendingData, preloader);
            } else {
                self.showErrorsPanel();
            }
        } else {
            if (notifyView.down('#userCombo').value !== null && form.isValid()) {
                sendingData.parameters = {};
                notifyView.down('#userCombo').clearInvalid();
                formErrorsPanel.hide();
                sendingData.id = self.actionId;
                sendingData.parameters.user = form.getValues().user.toString();
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Sending data",
                    target: notifyView
                });
                preloader.show();
                self.sendData(sendingData, preloader);
            } else {
                self.showErrorsPanel();
                notifyView.down('#userCombo').markInvalid('This is a required field');
            }
        }
    },

    sendData: function(sendingData, preloader) {
        var self = this;
        Ext.Ajax.request({
            url: '/api/isu/issue/' + self.issId + '/action',
            method: 'PUT',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/workspace/datacollection/issues';
                self.getApplication().fireEvent('acknowledge', 'Operation completed successfully!');
            },
            failure: function () {
                var title = 'Error',
                    message = 'Operation Failed!';

                self.getApplication().getController('Uni.controller.Error').showError(title, message);
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



