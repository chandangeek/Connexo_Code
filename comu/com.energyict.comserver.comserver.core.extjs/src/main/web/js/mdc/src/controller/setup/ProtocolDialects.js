/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.ProtocolDialects', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ProtocolDialectsOfDeviceConfiguration'
    ],

    views: [
        'setup.protocoldialect.ProtocolDialectSetup',
        'setup.protocoldialect.ProtocolDialectsGrid',
        'setup.protocoldialect.ProtocolDialectPreview',
        'setup.protocoldialect.ProtocolDialectEdit'
    ],

    stores: [
        'ProtocolDialectsOfDeviceConfiguration'
    ],

    refs: [
        {ref: 'protocolDialectsGrid', selector: '#protocoldialectsgrid'},
        {ref: 'protocolDialectPreviewForm', selector: '#protocolDialectPreviewForm'},
        {ref: 'protocolDialectPreview', selector: '#protocolDialectPreview'},
        {ref: 'protocolDialectPreviewTitle', selector: '#protocolDialectPreviewTitle'},
        {ref: 'protocolDialectEditView', selector: '#protocolDialectEdit'},
        {ref: 'protocolDialectEditForm', selector: '#protocolDialectEditForm'},
        {ref: 'protocolDialectsDetailsTitle', selector: '#protocolDialectsDetailsTitle'},
        {ref: 'restoreAllButton', selector: '#protocolDialectEdit #restoreAllButton'}
    ],

    init: function () {
      //  this.getProtocolDialectEdit().on('enableRestoreAll', this.enableRestoreAllButton(this));
        this.control({
            '#protocoldialectsgrid': {
                selectionchange: this.previewProtocolDialect
            },
            '#protocoldialectsgrid actioncolumn': {
                editProtocolDialect: this.editProtocolDialectHistory
            },
            '#protocolDialectPreview menuitem[action=editProtocolDialect]': {
                click: this.editProtocolDialectHistoryFromPreview
            },
            '#protocolDialectEdit #addEditButton[action=editProtocolDialect]': {
                click: this.editProtocolDialect
            },
            '#protocolDialectEdit #restoreAllButton[action=restoreAll]': {
                click: this.restoreAllDefaults
            },
            '#protocolDialectEdit property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            }
        });

    },

    editProtocolDialectHistory: function (record) {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/protocols/' + record.get('id') + '/edit';
    },

    showProtocolDialectsView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        if (mainView) mainView.setLoading(Uni.I18n.translate('general.loading', 'MDC', 'Loading...'));
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('protocolDialectSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        if (mainView) mainView.setLoading(false);
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#stepsMenu').setHeader(deviceConfig.get('name'));
                        //widget.down('#registerConfigTitle').html = '<h1>' + deviceConfigName + ' > ' + Uni.I18n.translate('registerConfig.registerConfigurations', 'MDC', 'Register configurations') + '</h1>';
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    previewProtocolDialect: function (grid, record) {
        var me = this,
            protocolDialect = record;
        if (protocolDialect.length === 1) {

            var protocolDialectName = protocolDialect[0].get('name');
            if (protocolDialect[0].propertiesStore.data.items.length > 0) {
                me.getProtocolDialectsDetailsTitle().setVisible(true);
                me.getProtocolDialectPreview().down('uni-button-action').setVisible(true);
            } else {
                me.getProtocolDialectsDetailsTitle().setVisible(false);
                me.getProtocolDialectPreview().down('uni-button-action').setVisible(false);
            }

            if (me.getProtocolDialectPreview().rendered) {
                me.getProtocolDialectPreview().down('property-form').loadRecord(protocolDialect[0]);
                me.getProtocolDialectPreviewForm().loadRecord(protocolDialect[0]);
            } else {
                me.getProtocolDialectPreview().on('afterrender', function() {
                    me.getProtocolDialectPreview().down('property-form').loadRecord(protocolDialect[0]);
                    me.getProtocolDialectPreviewForm().loadRecord(protocolDialect[0]);
                }, me, {single:true});
            }
            me.getProtocolDialectPreview().setTitle(protocolDialectName);
        }
    },

    addProtocolDialectHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/protocols/add';
    },

    editProtocolDialectHistoryFromPreview: function () {
        this.editProtocolDialectHistory(this.getProtocolDialectPreviewForm().getRecord());
    },

    showProtocolDialectsEditView: function (deviceTypeId, deviceConfigId, protocolDialectId) {
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigId;
        var me = this,
            returnlink,
            router = me.getController('Uni.controller.history.Router');

        if (me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens().indexOf('null') >= -1) {
            returnlink = '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigId + '/protocols';
        } else {
            returnlink = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
        }

        var widget = Ext.widget('protocolDialectEdit', {
            edit: true,
            returnLink: returnlink
        });

        widget.setLoading(true);
        var model = Ext.ModelManager.getModel('Mdc.model.ProtocolDialect');
        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        model.load(protocolDialectId, {
            success: function (protocolDialect) {
                if(protocolDialect.propertiesStore.count() === 0) {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                    return;
                } else {
                    me.getApplication().fireEvent('loadProtocolDialect', protocolDialect);
                    Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                            deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                            deviceConfigModel.load(deviceConfigId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    widget.down('form').loadRecord(protocolDialect);
                                    widget.down('property-form').loadRecordAsNotRequired(protocolDialect);
                                    widget.down('#protocolDialectEditAddTitle').update('<h1>' + Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'",[protocolDialect.get('name')]) + '</h1>');
                                    me.getApplication().fireEvent('changecontentevent', widget);
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                }
            }
        });
    },

    editProtocolDialect: function () {
        var record = this.getProtocolDialectEditForm().getRecord(),
            values = this.getProtocolDialectEditForm().getValues(),
            me = this,
            backUrl = me.getProtocolDialectEditView().returnLink;

        var propertyForm = me.getProtocolDialectEditView().down('property-form');
        if (record) {
            record.set(values);
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
            record.save({
                backUrl: backUrl,
                success: function (record) {
                    location.href = backUrl;
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('protocolDialect.acknowledgment', 'MDC', 'Protocol dialect saved') );
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getProtocolDialectEditForm().getForm().markInvalid(json.errors);
                        propertyForm.getForm().markInvalid(json.errors);
                    }
                }
            });
        }
    },

    showRestoreAllBtn: function(value) {
        var restoreBtn = this.getRestoreAllButton();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },

    restoreAllDefaults: function () {
        var me = this;
        me.getProtocolDialectEditView().down('property-form').restoreAll();
        me.getRestoreAllButton().disable();
    }
});

