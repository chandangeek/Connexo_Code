Ext.define('Mdc.controller.setup.ProtocolDialects', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ProtocolDialectsOfDeviceConfiguration',
        'Mdc.controller.setup.Properties',
        'Mdc.controller.setup.PropertiesView'
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
        {ref: 'protocolDialectsDetailsTitle', selector: '#protocolDialectsDetailsTitle'}
    ],

    init: function () {
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
            '#addEditButton[action=editProtocolDialect]': {
                click: this.editProtocolDialect
            },
            '#restoreAllButton[action=restoreAll]': {
                click: this.restoreAllDefaults
            }
        });
    },

    editProtocolDialectHistory: function (record) {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/protocols/' + record.get('id') + '/edit';
    },

    showProtocolDialectsView: function (deviceTypeId, deviceConfigurationId) {
        var me = this;
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
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        var deviceTypeName = deviceType.get('name');
                        var deviceConfigName = deviceConfig.get('name');
                        //widget.down('#registerConfigTitle').html = '<h1>' + deviceConfigName + ' > ' + Uni.I18n.translate('registerConfig.registerConfigurations', 'MDC', 'Register configurations') + '</h1>';
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getProtocolDialectsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    previewProtocolDialect: function (grid, record) {
        var protocolDialect = this.getProtocolDialectsGrid().getSelectionModel().getSelection();
        if (protocolDialect.length === 1) {
            this.getProtocolDialectPreviewForm().loadRecord(protocolDialect[0]);
            var protocolDialectName = protocolDialect[0].get('name');
            this.getProtocolDialectPreview().getLayout().setActiveItem(1);
            if (protocolDialect[0].propertiesStore.data.items.length > 0) {
                this.getProtocolDialectsDetailsTitle().setVisible(true);
            } else {
                this.getProtocolDialectsDetailsTitle().setVisible(false);
            }
            this.getPropertiesViewController().showProperties(protocolDialect[0], this.getProtocolDialectPreview());
            this.getProtocolDialectPreview().setTitle(protocolDialectName);
        } else {
            this.getProtocolDialectPreview().getLayout().setActiveItem(0);
        }
    },

    getPropertiesViewController: function () {
        return this.getController('Mdc.controller.setup.PropertiesView');
    },

    getPropertiesController: function () {
        return this.getController('Mdc.controller.setup.Properties');
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
        var me = this;
        var returnlink;

        if (me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens().indexOf('null') > -1) {
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
                                me.getPropertiesController().showProperties(protocolDialect, widget);
                                widget.down('#protocolDialectEditAddTitle').update('<h1>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + protocolDialect.get('name') + '\'</h1>');
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.setLoading(false);
                            }
                        });
                    }
                });
            }
        });
    },

    editProtocolDialect: function () {
        var record = this.getProtocolDialectEditForm().getRecord(),
            values = this.getProtocolDialectEditForm().getValues(),
            me = this;

        if (record) {
            record.set(values);
            record.propertiesStore = me.getPropertiesController().updateProperties();
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/protocols';
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('protocolDialect.acknowlegment', 'MDC', 'Protocol dialect has been saved') );
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getProtocolDialectEditForm().getForm().markInvalid(json.errors);
                        me.getPropertiesController().showErrors(json.errors);
                    }
                }
            });
        }
    },

    restoreAllDefaults: function () {
        this.getPropertiesController().restoreAllDefaults();
    }

});

