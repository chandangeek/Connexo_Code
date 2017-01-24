Ext.define('Mdc.controller.setup.DeviceGeneralAttributes', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.devicegeneralattributes.Setup',
        'setup.devicegeneralattributes.Edit'
    ],

    requires: [
        'Mdc.model.DeviceGeneralAttributes'
    ],

    refs: [
        {ref: 'displayPropertyForm', selector: '#deviceGeneralAttributesSetup property-form'},
        {ref: 'editPropertyForm', selector: '#deviceGeneralAttributesEdit property-form'},
        {ref: 'restoreToDefaultBtn', selector: '#deviceGeneralAttributesEdit #deviceGeneralAttributesRestoreDefaultBtn'},
        {ref: 'deviceGeneralAttributesEdit', selector: '#deviceGeneralAttributesEdit'}
    ],

    init: function () {
        this.control({
            '#deviceGeneralAttributesEdit #deviceGeneralAttributesSaveBtn': {
                click: this.saveGeneralAttributes
            },
            '#deviceGeneralAttributesEdit #deviceGeneralAttributesCancelBtn': {
                click: this.moveToPreviousPage
            },
            '#deviceGeneralAttributesEdit #deviceGeneralAttributesRestoreDefaultBtn': {
                click: this.restoreDefault
            },
            '#deviceGeneralAttributesEdit property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            }
        });
    },

    showRestoreAllBtn: function (value) {
        var restoreBtn = this.getRestoreToDefaultBtn();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },

    saveGeneralAttributes: function () {
        var me = this,
            form = me.getEditPropertyForm(),
            editView = me.getDeviceGeneralAttributesEdit(),
            formErrorsPanel = editView.down('#form-errors');

        editView.setLoading();

        form.updateRecord();
        if (!form.isValid()) {
            formErrorsPanel.show();
            editView.setLoading(false);
        } else {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            form.getRecord().save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('devices/device/generalattributes').buildUrl(),
                success: function () {
                    editView.setLoading(false);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('generalAttributes.saved', 'MDC', 'General attributes saved.'));
                    me.moveToPreviousPage();

                },
                failure: function (record, operation) {
                    if (operation.response.status == 400) {
                        if (!Ext.isEmpty(operation.response.responseText)) {
                            var json = Ext.decode(operation.response.responseText, true);
                            if (json && json.errors) {
                                formErrorsPanel.show();
                                form.getForm().markInvalid(json.errors);
                            }
                        }
                    }
                    editView.setLoading(false);
                }
            });
        }
    },

    moveToPreviousPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('devices/device/generalattributes').forward();
    },

    restoreDefault: function () {
        this.getEditPropertyForm().restoreAll();
    },

    showGeneralAttributesView: function (deviceId) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            viewPort = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewPort.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                widget = Ext.widget('deviceGeneralAttributesSetup', {device: device, router: router});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.loadPropertiesRecord(deviceId, widget);
                viewPort.setLoading(false);
            }
        });
    },

    showEditGeneralAttributesView: function (deviceId) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            viewPort = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewPort.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                widget = Ext.widget('deviceGeneralAttributesEdit', {device: device, router: router});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.loadPropertiesRecord(deviceId, widget);
                viewPort.setLoading(false);
            }
        });
    },

    loadPropertiesRecord: function (deviceId, widget) {
        var model = Ext.ModelManager.getModel('Mdc.model.DeviceGeneralAttributes'),
            form = widget.down('property-form'),
            idProperty = 1;

        widget.setLoading();

        model.getProxy().setExtraParam('deviceId', deviceId);
        model.load(idProperty, {
            success: function (generalAttribute) {
                form.loadRecord(generalAttribute);
                widget.setLoading(false);
            }
        });
    }
});