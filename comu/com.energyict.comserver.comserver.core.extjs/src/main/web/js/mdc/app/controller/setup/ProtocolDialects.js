Ext.define('Mdc.controller.setup.ProtocolDialects', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Uni.model.BreadcrumbItem',
        'Mdc.store.ProtocolDialectsOfDeviceConfiguration',
        'Mdc.controller.setup.Properties',
        'Mdc.controller.setup.PropertiesView'
    ],

    views: [
        'setup.Browse',
        'setup.protocoldialect.ProtocolDialectSetup',
        'setup.protocoldialect.ProtocolDialectsGrid',
        'setup.protocoldialect.ProtocolDialectPreview',
        'setup.protocoldialect.ProtocolDialectEdit'
    ],

    stores: [
        'ProtocolDialectsOfDeviceConfiguration'
    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'protocoldialectssgrid', selector: '#protocoldialectsgrid'},
        {ref: 'protocolDialectPreviewForm', selector: '#protocolDialectPreviewForm'},
        {ref: 'protocolDialectPreview', selector: '#protocolDialectPreview'},
        {ref: 'protocolDialectPreviewTitle', selector: '#protocolDialectPreviewTitle'},
        {ref: 'protocolDialectEditView', selector: '#protocolDialectEdit'},
        {ref: 'protocolDialectEditForm', selector: '#protocolDialectEditForm'}
    ],

    init: function () {
        this.control({
            '#protocoldialectsgrid': {
                selectionchange: this.previewProtocolDialect
            },
            '#protocoldialectsgrid actioncolumn': {
                editItem: this.editProtocolDialectHistory
                // deleteItem: this.deleteRegisterConfiguration
            },
            '#protocolDialectPreview menuitem[action=editProtocolDialect]': {
                click: this.editProtocolDialectHistoryFromPreview
            }
        });
    },

    editProtocolDialectHistory: function (record) {
        location.href = '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/protocols/' + record.get('id') + '/edit';
    },

    showProtocolDialectsView: function (deviceTypeId, deviceConfigurationId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('protocolDialectSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        var deviceTypeName = deviceType.get('name');
                        var deviceConfigName = deviceConfig.get('name');
                        //widget.down('#registerConfigTitle').html = '<h1>' + deviceConfigName + ' > ' + Uni.I18n.translate('registerConfig.registerConfigurations', 'MDC', 'Register configurations') + '</h1>';
                        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                        me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, deviceTypeName, deviceConfigName);
                    }
                });
            }
        });
    },

    previewProtocolDialect: function (grid, record) {
        var protocolDialect = this.getProtocoldialectssgrid().getSelectionModel().getSelection();
        if (protocolDialect.length === 1) {
            this.getProtocolDialectPreviewForm().loadRecord(protocolDialect[0]);
            var protocolDialectName = protocolDialect[0].get('name');
            this.getProtocolDialectPreview().getLayout().setActiveItem(1);
            this.getProtocolDialectPreviewTitle().update('<h4>' + protocolDialectName + '</h4>');
            this.getProtocolDialectPreviewForm().loadRecord(protocolDialect[0]);
            this.getPropertiesViewController().showProperties(protocolDialect[0], this.getProtocolDialectPreview());
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
        location.href = '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/protocols/add';
    },

    editProtocolDialectHistoryFromPreview: function () {
        this.editProtocolDialectHistory(this.getProtocolDialectPreviewForm().getRecord());
    },


    overviewBreadCrumbs: function (deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName) {
        var me = this;

        var breadcrumbRegisterConfigurations = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('protocoldialect.protocols', 'MDC', 'Protocols'),
            href: 'protocols'

        });

        var breadcrumbDeviceConfig = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceConfigName,
            href: deviceConfigId
        });

        var breadcrumbDeviceConfigs = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.deviceConfigs', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });

        breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbRegisterConfigurations);

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    showProtocolDialectsEditView: function(deviceTypeId,deviceConfigId,protocolDialectId){
            this.deviceTypeId = deviceTypeId;
            this.deviceConfigurationId = deviceConfigId;

            var me = this;

            var widget = Ext.widget('protocolDialectEdit', {
                edit: true,
                returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
            });

            widget.setLoading(true);
            var model = Ext.ModelManager.getModel('Mdc.model.ProtocolDialect');
            model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
            model.load(protocolDialectId, {
                success: function (protocolDialect) {
                    Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                        success: function (deviceType) {
                            var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                            deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                            deviceConfigModel.load(deviceConfigId, {
                                success: function (deviceConfiguration) {
                                    me.editBreadCrumb(deviceTypeId, deviceConfigId, protocolDialectId, deviceType.get('name'), deviceConfiguration.get('name'), protocolDialect.get('name'));
                                    widget.down('form').loadRecord(protocolDialect);
                                    me.getPropertiesController().showProperties(protocolDialect, widget);
                                    widget.down('#protocolDialectEditAddTitle').update('<H2>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + protocolDialect.get('name') + '"</H2>');
                                    me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                }
            });
        },

    editBreadCrumb: function (deviceTypeId, deviceConfigId, protocolDialectId, deviceTypeName, deviceConfigName, protocolDialectName) {
           var me = this;

           var breadcrumbEdit = Ext.create('Uni.model.BreadcrumbItem', {
               text: Uni.I18n.translate('protocolDialect.edit', 'MDC', 'Edit protocol'),
               href: 'edit'
           });

           var breadcrumbProtocolDialects = Ext.create('Uni.model.BreadcrumbItem', {
               text: Uni.I18n.translate('protocolDialect.protocolDialects', 'MDC', 'Protocol dialects'),
               href: 'protocols'
           });

           var breadcrumbDeviceConfig = Ext.create('Uni.model.BreadcrumbItem', {
               text: deviceConfigName,
               href: deviceConfigId
           });

           var breadcrumbDeviceConfigs = Ext.create('Uni.model.BreadcrumbItem', {
               text: Uni.I18n.translate('registerConfig.deviceConfigs', 'MDC', 'Device configurations'),
               href: 'deviceconfigurations'
           });

           var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
               text: deviceTypeName,
               href: deviceTypeId
           });

           var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
               text: Uni.I18n.translate('registerConfig.deviceTypes', 'MDC', 'Device types'),
               href: 'devicetypes'
           });
           var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
               text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
               href: '#setup'
           });

           breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbProtocolDialects).setChild(breadcrumbEdit);

           me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
       }

});

