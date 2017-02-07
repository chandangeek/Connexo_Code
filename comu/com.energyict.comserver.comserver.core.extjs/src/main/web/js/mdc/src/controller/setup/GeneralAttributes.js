/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.GeneralAttributes', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.generalattributes.Setup',
        'setup.generalattributes.Edit'
    ],

    requires: [
        'Mdc.model.GeneralAttributes'
    ],

    refs: [
        {ref: 'displayPropertyForm', selector: '#generalAttributesSetup property-form'},
        {ref: 'editPropertyForm', selector: '#generalAttributesEdit property-form'},
        {ref: 'restoreToDefaultBtn', selector: '#generalAttributesEdit #generalAttributesRestoreDefaultBtn'},
        {ref: 'generalAttributesEdit', selector: '#generalAttributesEdit'}
    ],

    init: function () {
        this.control({
            '#generalAttributesEdit #generalAttributesSaveBtn': {
                click: this.saveGeneralAttributes
            },
            '#generalAttributesEdit #generalAttributesCancelBtn': {
                click: this.moveToPreviousPage
            },
            '#generalAttributesEdit #generalAttributesRestoreDefaultBtn': {
                click: this.restoreDefault
            },
            '#generalAttributesEdit property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            }
        });
    },

    showRestoreAllBtn: function(value) {
        var restoreBtn = this.getRestoreToDefaultBtn();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },

    saveGeneralAttributes: function() {
        var me = this,
            form = me.getEditPropertyForm(),
            editView = me.getGeneralAttributesEdit(),
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
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/generalattributes').buildUrl(),
                callback: function (model, operation, success) {

                    if (success) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('generalAttributes.saved', 'MDC', 'General attributes saved.'));
                        me.moveToPreviousPage();
                    }
                    else {
                        var json = Ext.decode(operation.response.responseText);
                        if (operation.response.status === 400) {
                            if (json && json.errors) {
                                form.getForm().markInvalid(json.errors);
                            }
                            formErrorsPanel.show();
                        }
                    }

                }
            });
        }
    },

    moveToPreviousPage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/generalattributes').forward();
    },

    restoreDefault: function() {
        this.getEditPropertyForm().restoreAll();
    },

    showGeneralAttributesView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget;

        widget = Ext.widget('generalAttributesSetup', { router: router });
        me.getApplication().fireEvent('changecontentevent', widget);
        me.loadPropertiesRecord(deviceTypeId, deviceConfigurationId, widget);
    },

    showEditGeneralAttributesView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget;

        widget = Ext.widget('generalAttributesEdit', { router: router });
        me.getApplication().fireEvent('changecontentevent', widget);
        me.loadPropertiesRecord(deviceTypeId, deviceConfigurationId, widget);
    },


    loadPropertiesRecord: function(deviceTypeId, deviceConfigurationId, widget) {
        var model = Ext.ModelManager.getModel('Mdc.model.GeneralAttributes'),
            form = widget.down('property-form'),
            idProperty = 1;

        widget.setLoading();

        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        model.load(idProperty, {
            success: function (generalAttribute) {
                form.loadRecord(generalAttribute);
                widget.setLoading(false);
            }
        });

        this.loadDeviceTypeAndConfiguration(deviceTypeId, deviceConfigurationId, widget);
    },

    loadDeviceTypeAndConfiguration: function (deviceTypeId, deviceConfigurationId, widget) {
        var me = this;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                    }
                });
            }
        });
    }

});