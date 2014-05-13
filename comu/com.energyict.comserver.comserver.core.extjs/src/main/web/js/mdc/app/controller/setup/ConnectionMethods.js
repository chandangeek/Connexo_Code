Ext.define('Mdc.controller.setup.ConnectionMethods', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ConnectionMethodsOfDeviceConfiguration',
        'Uni.model.BreadcrumbItem',
        'Mdc.controller.setup.Properties',
        'Mdc.controller.setup.PropertiesView'
    ],

    views: [
        'setup.Browse',
        'setup.connectionmethod.ConnectionMethodSetup',
        'setup.connectionmethod.ConnectionMethodsGrid',
        'setup.connectionmethod.ConnectionMethodPreview',
        'setup.connectionmethod.ConnectionMethodEdit'
    ],

    stores: [
        'ConnectionMethodsOfDeviceConfiguration',
        'ConnectionTypes',
        'ConnectionStrategies'
    ],

    refs: [
        {ref: 'connectionmethodsgrid', selector: '#connectionmethodsgrid'},
        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'},
        {ref: 'connectionMethodPreview', selector: '#connectionMethodPreview'},
        {ref: 'connectionMethodPreviewTitle', selector: '#connectionMethodPreviewTitle'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'},
        {ref: 'connectionMethodEditView',selector: '#connectionMethodEdit'},
        {ref: 'connectionMethodEditForm',selector: '#connectionMethodEditForm'},
        {ref: 'connectionStrategyComboBox',selector: '#connectionStrategyComboBox'},
        {ref: 'scheduleField',selector: '#scheduleField'},
        {ref: 'connectionTypeComboBox',selector: '#connectionTypeComboBox'}
    ],

    init: function () {
        this.control({
                '#connectionmethodsgrid': {
                    selectionchange: this.previewConnectionMethod
                },
                'button[action = createOutboundConnectionMethod]': {
                    click: this.addOutboundConnectionMethodHistory
                },
                'button[action = createInboundConnectionMethod]': {
                    click: this.addInboundConnectionMethodHistory
                },
                '#connectionmethodsgrid actioncolumn': {
                    editItem: this.editConnectionMethodHistory,
                    deleteItem: this.deleteConnectionMethod,
                    toggleDefault: this.toggleDefaultConnectionMethod
                },
                '#connectionMethodPreview menuitem[action=editConnectionMethod]': {
                    click: this.editConnectionMethodHistoryFromPreview
                },
                '#connectionTypeComboBox': {
                    select: this.showConnectionTypeProperties
                },
                '#addEditButton[action=addOutboundConnectionMethod]':{
                    click: this.addOutboundConnectionMethod
                },
                '#addEditButton[action=editOutboundConnectionMethod]':{
                    click: this.editOutboundConnectionMethod
                },
                '#addEditButton[action=addInboundConnectionMethod]':{
                    click: this.addInboundConnectionMethod
                },
                '#addEditButton[action=editInboundConnectionMethod]':{
                    click: this.editInboundConnectionMethod
                },
                '#connectionStrategyComboBox': {
                    select: this.showScheduleField
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
                        widget.down('#connectionMethodTitle').html = '<h1>' + deviceConfigName + ' > ' + Uni.I18n.translate('connectionmethod.connectionmethods', 'MDC', 'Connection methods') + '</h1>';
                        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                        me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, deviceTypeName, deviceConfigName);
                    }
                });
            }
        });
    },

    previewConnectionMethod: function(){
        var connectionMethod = this.getConnectionmethodsgrid().getSelectionModel().getSelection();
        if (connectionMethod.length == 1) {
            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            var connectionMethodName = connectionMethod[0].get('name');
            this.getConnectionMethodPreview().getLayout().setActiveItem(1);
            this.getConnectionMethodPreviewTitle().update('<h4>' + connectionMethodName + '</h4>');
            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            this.getPropertiesViewController().showProperties(connectionMethod[0], this.getConnectionMethodPreview());
        } else {
            this.getConnectionMethodPreview().getLayout().setActiveItem(0);
        }
    },

    getPropertiesViewController: function () {
        return this.getController('Mdc.controller.setup.PropertiesView');
    },


    addOutboundConnectionMethodHistory: function(){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ this.deviceConfigurationId + '/connectionmethods/addoutbound';
    },

    addInboundConnectionMethodHistory: function(){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ this.deviceConfigurationId + '/connectionmethods/addinbound';
    },

    editConnectionMethodHistory: function(record){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ this.deviceConfigurationId + '/connectionmethods/'+ record.get('id')+'/edit';
    },

    editConnectionMethodHistoryFromPreview: function(){
        this.editConnectionMethodHistory(this.getConnectionMethodPreviewForm().getRecord());
    },

    showAddConnectionMethodView:function(deviceTypeId,deviceConfigId,direction){
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
        var comPortPoolStore = Ext.StoreManager.get('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
        var me=this;
        this.deviceTypeId=deviceTypeId;
        this.deviceConfigurationId=deviceConfigId;
        var widget = Ext.widget('connectionMethodEdit', {
            edit: false,
            returnLink: '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+this.deviceConfigurationId+'/connectionmethods',
            connectionTypes: connectionTypesStore,
            comPortPools: comPortPoolStore,
            connectionStrategies: connectionStrategiesStore,
            direction: direction
        });
        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        comPortPoolStore.filter('direction', direction);
                        comPortPoolStore.load({
                            callback: function(){
                                connectionStrategiesStore.load({
                                     callback: function(){
                                         connectionTypesStore.getProxy().setExtraParam('protocolId', deviceType.get('communicationProtocolId'));
                                         connectionTypesStore.load({
                                             callback: function(){
                                                 var deviceTypeName = deviceType.get('name');
                                                 var deviceConfigName = deviceConfig.get('name');
                                                 var title = direction==='Outbound'?Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'):Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method');
                                                 title = deviceConfigName + ' > ' + title;
                                                 widget.down('#connectionMethodEditAddTitle').update('<H2>' + title  + '</H2>');
                                                 me.createBreadCrumbs(deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName, direction, 'add');
                                                 widget.setLoading(false);
                                             }
                                         });
                                     }
                                });
                            }
                        });
                    }
                });
            }
        });
    },

    showScheduleField: function(combobox,objList){
         if(objList[0].get('connectionStrategy')==='minimizeConnections'){
             this.getScheduleField().setVisible(true);
         } else {
             this.getScheduleField().setVisible(false);
             this.getScheduleField().clear();
         }
    },

    addOutboundConnectionMethod: function(){
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Outbound'
        this.addConnectionMethod(values);

    },

    addInboundConnectionMethod: function(){
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Inbound';
        this.addConnectionMethod(values);
    },

    editOutboundConnectionMethod: function(){
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Outbound';
        this.editConnectionMethod(values);
    },

    editInboundConnectionMethod: function(){
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Inbound';
        this.editConnectionMethod(values);
    },

    editConnectionMethod: function(values){
        var record = this.getConnectionMethodEditForm().getRecord();
        this.updateRecord(record,values);

    },

    addConnectionMethod: function(values){
        var record = Ext.create(Mdc.model.ConnectionMethod);
        this.updateRecord(record,values);
    },

    updateRecord: function(record, values){
        var me = this;
        if (record) {
            record.set(values);
            if(values.connectionStrategy==='asSoonAsPossible'){
                record.set('nextExecutionSpecs',null);
            }
            record.propertiesStore = this.getPropertiesController().updateProperties();
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            record.save({
                success: function (record) {
                    location.href = '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/connectionmethods';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getConnectionMethodEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    deleteConnectionMethod: function(connectionMethodToDelete){
        var me = this;
            Ext.MessageBox.show({
                msg: Uni.I18n.translate('connectionmethod.deleteConnectionMethod', 'MDC', 'Are you sure you want to delete connection method ' + connectionMethodToDelete.get('name') + '?'),
                title: Uni.I18n.translate('connectionmethod.deleteConnectionMethod.title', 'MDC', 'Delete connection method ') + ' ' + connectionMethodToDelete.get('name') + '?',
                config: {
                    connectionMethodToDelete: connectionMethodToDelete,
                    me: me
                },
                buttons: Ext.MessageBox.YESNO,
                fn: me.removeConnectionMethod,
                icon: Ext.MessageBox.WARNING
            });
    },

    removeConnectionMethod: function(btn, text, opt){
        if (btn === 'yes') {
            var connectionMethodToDelete = opt.config.connectionMethodToDelete;
            var me = opt.config.me;
            connectionMethodToDelete.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            connectionMethodToDelete.destroy({
                callback: function () {
                    location.href = '#setup/devicetypes/'+me.deviceTypeId+'/deviceconfigurations/'+me.deviceConfigurationId+'/connectionmethods';
                }
            });

        }
    },

    showConnectionTypeProperties: function(combobox,objList){
        this.getPropertiesController().showProperties(this.getConnectionTypeComboBox().findRecordByValue(this.getConnectionTypeComboBox().getValue()),this.getConnectionMethodEditView(),false);
    },

    getPropertiesController: function () {
        return this.getController('Mdc.controller.setup.Properties');
    },

    showConnectionMethodEditView: function(deviceTypeId,deviceConfigId,connectionMethodId){
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
        var comPortPoolStore = Ext.StoreManager.get('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigId;
        var me = this;

        var model = Ext.ModelManager.getModel('Mdc.model.ConnectionMethod');
        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        model.load(connectionMethodId, {
            success: function (connectionMethod) {
                var widget = Ext.widget('connectionMethodEdit', {
                    edit: true,
                    returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens(),
                    connectionTypes: connectionTypesStore,
                    comPortPools: comPortPoolStore,
                    connectionStrategies: connectionStrategiesStore,
                    direction: connectionMethod.get('direction')
                });

                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                widget.setLoading(true);

                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigId, {
                            success: function (deviceConfig) {
                                comPortPoolStore.filter('direction', connectionMethod.get('direction'));
                                comPortPoolStore.load({
                                    callback: function(){
                                        connectionStrategiesStore.load({
                                            callback: function(){
                                                connectionTypesStore.getProxy().setExtraParam('protocolId', deviceType.get('communicationProtocolId'));
                                                connectionTypesStore.load({
                                                    callback: function(){
                                                        var deviceTypeName = deviceType.get('name');
                                                        var deviceConfigName = deviceConfig.get('name');
                                                        if(connectionMethod.get('connectionStrategy')==='minimizeConnections'){
                                                            widget.down('form').down('#scheduleField').setVisible(true);
                                                        }
                                                        widget.down('form').loadRecord(connectionMethod);
                                                        var title = connectionMethod.get('direction')==='Outbound'?Uni.I18n.translate('connectionmethod.editOutboundConnectionMethod', 'MDC', 'Edit outbound connection method'):Uni.I18n.translate('connectionmethod.editInboundConnectionMethod', 'MDC', 'Edit inbound connection method');
                                                        title = deviceConfigName + ' > ' + title;
                                                        widget.down('#connectionMethodEditAddTitle').update('<H2>' + title + '</H2>');
                                                        widget.down('form').down('#connectionTypeComboBox').setValue(connectionMethod.get('connectionType'));
                                                        widget.down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
                                                        widget.down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
                                                        me.getPropertiesController().showProperties(connectionMethod,me.getConnectionMethodEditView(),false);
                                                        me.createBreadCrumbs(deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName, connectionMethod.get('direction'), 'edit');
                                                        widget.setLoading(false);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    },

    toggleDefaultConnectionMethod: function(connectionMethod){
        var me=this;
        if(connectionMethod.hasOwnProperty('action')){
            connectionMethod = this.getConnectionmethodsgrid().getSelectionModel().getSelection()[0];
        }
        if(connectionMethod.get('isDefault')===true){
            connectionMethod.set('isDefault',false);
        } else {
            connectionMethod.set('isDefault',true);
        }
        if(connectionMethod.get('connectionStrategy')==='asSoonAsPossible' || connectionMethod.get('direction')==='Inbound'){
            connectionMethod.set('nextExecutionSpecs',null);
        }
        this.getPropertiesController().updatePropertiesWithoutView(connectionMethod);
        connectionMethod.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
        connectionMethod.save({
            callback: function(){
                me.previewConnectionMethod();
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

    createBreadCrumbs: function (deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName, direction, action) {
        var me = this;
        var directionText;
        var createEditHref;
        if(action === 'add'){
            createEditHref = 'create';
            directionText = direction==='Outbound'?Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'):Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method');
        } else if (action === 'edit'){
            createEditHref = 'edit';
            directionText = direction==='Outbound'?Uni.I18n.translate('connectionmethod.editOutboundConnectionMethod', 'MDC', 'Edit outbound connection method'):Uni.I18n.translate('connectionmethod.editInboundConnectionMethod', 'MDC', 'Edit inbound connection method');
        }

        var breadcrumbCreate = Ext.create('Uni.model.BreadcrumbItem', {
            text: directionText,
            href: createEditHref
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