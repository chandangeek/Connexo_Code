Ext.define('Mdc.controller.setup.ConnectionMethods', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ConnectionMethodsOfDeviceConfiguration',
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'setup.Browse',
        'setup.connectionmethod.ConnectionMethodSetup',
        'setup.connectionmethod.ConnectionMethodsGrid',
        'setup.connectionmethod.ConnectionMethodPreview',
        'setup.connectionmethod.ConnectionMethodEdit',
        'setup.connectionmethod.ConnectionMethodDetail'
    ],

    stores: [
        'ConnectionMethodsOfDeviceConfiguration'
    ],

    refs: [
        {ref: 'connectionmethodsgrid', selector: '#connectionmethodsgrid'},
        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'},
        {ref: 'connectionMethodPreview', selector: '#connectionMethodPreview'},
        {ref: 'connectionMethodPreviewTitle', selector: '#connectionMethodPreviewTitle'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'}
    ],

    init: function () {
        this.control({
                '#connectionmethodsgrid': {
                    selectionchange: this.previewConnectionMethod
                },
                '#connectionmethodsgrid button[action = createConnectionMethod]': {
                    click: this.addConnectionMethodHistory
                },
                '#connectionmethodsgrid actioncolumn': {
                    editItem: this.editConnectionMethodHistory
                   // deleteItem: this.deleteRegisterConfiguration
                },
                '#connectionMethodPreview menuitem[action=editConnectionMethod]': {
                    click: this.editConnectionMethodHistoryFromPreview
                }
        });
    },

    showConnectionMethods: function(deviceTypeId,deviceConfigurationId){
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('connectionMethodSetup', {deviceTypeId: deviceTypeId,deviceConfigId:deviceConfigurationId});
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

    previewConnectionMethod: function(grid,record){
        console.log(connectionMethod);
        var connectionMethod = this.getConnectionmethodsgrid().getSelectionModel().getSelection();
        if (connectionMethod.length == 1) {
            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            var connectionMethodName = connectionMethod[0].get('name');
            this.getConnectionMethodPreview().getLayout().setActiveItem(1);
            this.getConnectionMethodPreviewTitle().update('<h4>' + connectionMethodName + '</h4>');
            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
        } else {
            this.getConnectionMethodPreview().getLayout().setActiveItem(0);
        }
    },

    addConnectionMethodHistory: function(){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ this.deviceConfigurationId + '/connectionmethods/add';
    },

    editConnectionMethodHistory: function(record){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ this.deviceConfigurationId + '/connectionmethods/'+ record.get('id')+'/edit';
    },

    editConnectionMethodHistoryFromPreview: function(){
        this.editConnectionMethodHistory(this.getConnectionMethodPreviewForm().getRecord());
    },

    showConnectionMethodCreateView:function(deviceTypeId,deviceConfigId){
        var me=this;
        this.deviceTypeId=deviceTypeId;
        this.deviceConfigurationId=deviceConfigId;
        var widget = Ext.widget('connectionMethodEdit', {
            edit: false,
            returnLink: '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+this.deviceConfigurationId+'/connectionmethods'
        });
        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        var deviceTypeName = deviceType.get('name');
                        var deviceConfigName = deviceConfig.get('name');
                        widget.down('#connectionMethodEditAddTitle').update('<H2>' + Uni.I18n.translate('connectionmethod.addConnectionMethod', 'MDC', 'Add connection method') + '</H2>');
                        me.createBreadCrumbs(deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName);
                    }
                });
            }
        });
    },

    showConnectionMethodEditView: function(deviceTypeId,deviceConfigId,connectionMethodId){
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigId;
        var me = this;

        var widget = Ext.widget('connectionMethodEdit', {
            edit: true,
            returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
        });

        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        var model = Ext.ModelManager.getModel('Mdc.model.ConnectionMethod');
        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        model.load(connectionMethodId, {
            success: function (connectionMethod) {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                        deviceConfigModel.load(deviceConfigId, {
                            success: function (deviceConfiguration) {
//                                me.editBreadCrumb(deviceTypeId, deviceConfigurationId, registerConfigurationId, deviceType.get('name'), deviceConfiguration.get('name'), registerConfiguration.get('name'));
                                widget.down('form').loadRecord(connectionMethod);
                                widget.down('#connectionMethodEditCreateTitle').update('<H2>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + connectionMethod.get('name') + '"</H2>');
                                widget.setLoading(false);
                            }
                        });
                    }
                });
            }
        });
    },

    overviewBreadCrumbs: function(deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName){
        var me = this;

        var breadcrumbRegisterConfigurations = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('connectionmethod.connectionmethods', 'MDC', 'Connection methods'),
            href: 'connectionmethods'

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

    createBreadCrumbs: function (deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName) {
        var me = this;

        var breadcrumbCreate = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('connectionmethod.addConnectionMethod', 'MDC', 'Add connection method'),
            href: 'create'
        });

        var breadcrumbRegisterConfigurations = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('connectionmethod.connectionmethods', 'MDC', 'Connection methods'),
            href: 'connectionmethods'

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

        breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbRegisterConfigurations).setChild(breadcrumbCreate);

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    }



});