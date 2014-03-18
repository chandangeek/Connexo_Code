Ext.define('Mdc.controller.setup.DeviceConfigurations', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    deviceTypeId: null,
    views: [
        'setup.deviceconfiguration.DeviceConfigurationsSetup',
        'setup.deviceconfiguration.DeviceConfigurationsGrid',
        'setup.deviceconfiguration.DeviceConfigurationPreview',
        'setup.deviceconfiguration.DeviceConfigurationDetail',
        'setup.deviceconfiguration.DeviceConfigurationEdit'

    ],

    stores: [
        'DeviceConfigurations'
    ],

    refs: [
        {ref: 'deviceConfigurationsGrid', selector: '#deviceconfigurationsgrid'},
        {ref: 'deviceConfigurationPreviewForm', selector: '#deviceConfigurationPreviewForm'},
        {ref: 'deviceConfigurationPreview', selector: '#deviceConfigurationPreview'},
        {ref: 'deviceConfigurationDetailsLink', selector: '#deviceConfigurationDetailsLink'},
        {ref: 'deviceConfigurationPreviewTitle', selector: '#deviceConfigurationPreviewTitle'},
        {ref: 'deviceConfigurationRegisterLink', selector: '#deviceConfigurationRegistersLink'},
        {ref: 'deviceConfigurationLogBookLink', selector: '#deviceConfigurationLogBooksLink'},
        {ref: 'deviceConfigurationLoadProfilesLink', selector: '#deviceConfigurationLoadProfilesLink'},
        {ref: 'deviceConfigurationDetailRegisterLink', selector: '#deviceConfigurationDetailRegistersLink'},
        {ref: 'deviceConfigurationDetailLogBookLink', selector: '#deviceConfigurationDetailLogBooksLink'},
        {ref: 'deviceConfigurationDetailLoadProfilesLink', selector: '#deviceConfigurationDetailLoadProfilesLink'},
        {ref: 'deviceConfigurationDetailDeviceTypeLink', selector: '#deviceConfigurationDetailDeviceTypeLink'},
        {ref: 'deviceConfigurationDetailForm', selector: '#deviceConfigurationDetailForm'},
        {ref: 'deviceConfigurationEditForm', selector: '#deviceConfigurationEditForm'},
        {ref: 'activateDeviceconfigurationMenuItem', selector: '#activateDeviceconfigurationMenuItem'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'}
    ],

    init: function () {
        this.control({
            '#deviceconfigurationsgrid': {
                selectionchange: this.previewDeviceConfiguration
            },
            '#deviceConfigurationsSetup button[action = createDeviceConfiguration]': {
                click: this.createDeviceConfigurationHistory
            },
            '#deviceconfigurationsgrid actioncolumn': {
                editItem: this.editDeviceConfigurationHistory,
                deleteItem: this.deleteDeviceConfiguration,
                activateItem: this.activateDeviceConfiguration
            },
            '#deviceConfigurationPreview menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationHistory
            },
            '#deviceConfigurationPreview menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfiguration
            },
            '#deviceConfigurationPreview menuitem[action=activateDeactivateDeviceConfiguration]': {
                click: this.activateDeviceConfiguration
            },
            '#deviceConfigurationDetail menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromDetails
            },
            '#deviceConfigurationDetail menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationFromDetailsHistory
            },
            '#deviceConfigurationDetail menuitem[action=activateDeactivateDeviceConfiguration]': {
                click: this.activateDeviceConfigurationFromDetails
            },
            '#createEditButton[action=createDeviceConfiguration]': {
                click: this.createDeviceConfiguration
            },
            '#createEditButton[action=editDeviceConfiguration]': {
                click: this.editDeviceConfiguration
            }
        });
    },

    showEditView: function (id) {

    },

    showDeviceConfigurations: function (id) {
        var me = this;
        this.deviceTypeId = id;
        this.getDeviceConfigurationsStore().getProxy().setExtraParam('deviceType', id);
        var widget = Ext.widget('deviceConfigurationsSetup', {deviceTypeId: id});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id,{
            success: function(deviceType){
                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                me.overviewBreadCrumb(id,deviceType.get('name'));
            }
        });

    },

    previewDeviceConfiguration: function (grid, record) {
        var deviceConfigurations = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection();
        if (deviceConfigurations.length == 1) {
            var deviceConfigurationId = deviceConfigurations[0].get('id');
            debugger;
            this.getActivateDeviceconfigurationMenuItem().setText(deviceConfigurations[0].get('active')===true?Uni.I18n.translate('general.deActivate', 'MDC', 'Deactivate'):Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
            this.getDeviceConfigurationRegisterLink().getEl().set({href: '#/setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations' + deviceConfigurationId + '/registertypes'});
            this.getDeviceConfigurationRegisterLink().getEl().setHTML(deviceConfigurations[0].get('registerCount') + ' ' + Uni.I18n.translate('devicetype.registers', 'MDC', 'register types'));
            this.getDeviceConfigurationLogBookLink().getEl().set({href: '#/setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations' + deviceConfigurationId + '/logbooks'});
            this.getDeviceConfigurationLogBookLink().getEl().setHTML(deviceConfigurations[0].get('logBookCount') + ' ' + Uni.I18n.translate('devicetype.logbooks', 'MDC', 'logbook types'));
            this.getDeviceConfigurationLoadProfilesLink().getEl().set({href: '#/setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations' + deviceConfigurationId + '/loadprofiles'});
            this.getDeviceConfigurationLoadProfilesLink().getEl().setHTML(deviceConfigurations[0].get('loadProfileCount') + ' ' + Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'load profile types'));
            this.getDeviceConfigurationPreviewForm().loadRecord(deviceConfigurations[0]);
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(1);
            this.getDeviceConfigurationDetailsLink().update('<a href="#/setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '">' + Uni.I18n.translate('general.viewDetails', 'MDC', 'View details') + '</a>');
            this.getDeviceConfigurationPreviewTitle().update('<h1>' + deviceConfigurations[0].get('name') + '</h1>');
        } else {
            this.getDeviceTypePreview().getLayout().setActiveItem(0);
        }
    },

    showDeviceConfigurationDetailsView: function (devicetype,deviceconfiguration) {
        var me = this;
        var widget = Ext.widget('deviceConfigurationDetail');
        var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
        this.deviceTypeId=devicetype
        deviceConfigModel.getProxy().setExtraParam('deviceType', devicetype);
        deviceConfigModel.load(deviceconfiguration, {
            success: function (deviceConfiguration) {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(devicetype,{
                    success: function(deviceType){
                        var deviceConfigurationId = deviceConfiguration.get('id');
                        me.detailBreadCrumb(devicetype,deviceType.get('name'),deviceConfigurationId,deviceConfiguration.get('name'));
                        me.getDeviceConfigurationDetailDeviceTypeLink().getEl().set({href:'#/setup/devicetypes/' + me.deviceTypeId});
                        me.getDeviceConfigurationDetailDeviceTypeLink().getEl().setHTML(deviceType.get('name'));
                        me.getDeviceConfigurationDetailRegisterLink().getEl().set({href: '#/setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations' + deviceConfigurationId +'/registertypes'});
                        me.getDeviceConfigurationDetailRegisterLink().getEl().setHTML(deviceConfiguration.get('registerCount') + ' ' + Uni.I18n.translate('devicetype.registers', 'MDC', 'register types'));
                        me.getDeviceConfigurationDetailLogBookLink().getEl().set({href: '#/setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations' + deviceConfigurationId + '/logbooks'});
                        me.getDeviceConfigurationDetailLogBookLink().getEl().setHTML(deviceConfiguration.get('logBookCount') + ' ' + Uni.I18n.translate('devicetype.logbooks', 'MDC', 'logbook types'));
                        me.getDeviceConfigurationDetailLoadProfilesLink().getEl().set({href: '#/setup/devicetypes/' + me.deviceTypeId +  '/deviceconfigurations' + deviceConfigurationId + '/loadprofiles'});
                        me.getDeviceConfigurationDetailLoadProfilesLink().getEl().setHTML(deviceConfiguration.get('loadProfileCount') + ' ' + Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'load profile types'));
                        me.getDeviceConfigurationPreviewTitle().update('<h1>' + deviceConfiguration.get('name') + ' - ' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>');
                        widget.down('form').loadRecord(deviceConfiguration);
                    }
                });
            }
        });
        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    createDeviceConfigurationHistory: function(){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/create';
    },

    editDeviceConfigurationHistory: function(){
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0].get('id') +'/edit';
    },

    editDeviceConfigurationFromDetailsHistory: function(){
        var record = this.getDeviceConfigurationDetailForm().getRecord();
        location.href = '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations/'+ record.get('id') +'/edit';
    },

    activateDeviceConfiguration: function(){
        var me=this;
       var deviceConfigurationToActivateDeactivate = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0];
        if(deviceConfigurationToActivateDeactivate.get('active')===true){
            deviceConfigurationToActivateDeactivate.set('active',false);
        } else {
            deviceConfigurationToActivateDeactivate.set('active',true);
        }
        deviceConfigurationToActivateDeactivate.getProxy().setExtraParam('deviceType',this.deviceTypeId);
        deviceConfigurationToActivateDeactivate.save({
            callback: function(){
                location.href = '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations';
            }
        });
    },

    activateDeviceConfigurationFromDetails: function(){
        var me = this;
        debugger;
        var deviceConfigurationToActivateDeactivate = this.getDeviceConfigurationDetailForm().getRecord();
        if(deviceConfigurationToActivateDeactivate.get('active')===true){
            deviceConfigurationToActivateDeactivate.set('active',false);
        } else {
            deviceConfigurationToActivateDeactivate.set('active',true);
        }
        deviceConfigurationToActivateDeactivate.getProxy().setExtraParam('deviceType',this.deviceTypeId);
        deviceConfigurationToActivateDeactivate.save({
            callback: function(){
                location.href = '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/'+deviceConfigurationToActivateDeactivate.get('id');
            }
        });
    },

    deleteDeviceConfiguration: function(){
        var me = this;
        var deviceConfigurationToDelete = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0];
        deviceConfigurationToDelete.getProxy().setExtraParam('deviceType',this.deviceTypeId);
        deviceConfigurationToDelete.destroy({
            callback: function () {
                location.href = '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations';
            }
        });
    },

    deleteDeviceConfigurationFromDetails: function(){
        var me = this;
        var deviceConfigurationToDelete = this.getDeviceConfigurationDetailForm().getRecord();
        deviceConfigurationToDelete.getProxy().setExtraParam('deviceType',this.deviceTypeId);
        deviceConfigurationToDelete.destroy({
            callback: function () {
                location.href = '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations';
            }
        });
    },

    showDeviceConfigurationCreateView: function(deviceTypeId){
        var me=this;
        this.deviceTypeId=deviceTypeId;
        var widget = Ext.widget('deviceConfigurationEdit', {
            edit: false,
            returnLink: '#setup/devicetypes/'+this.deviceTypeId+'/deviceconfigurations'
        });

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId,{
            success: function(deviceType){
                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                widget.down('#deviceConfigurationEditCreateTitle').update('<H2>'+Uni.I18n.translate('general.create', 'MDC', 'Create') + ' ' + 'device configuration'+'</H2>');
                me.createBreadCrumb(deviceTypeId,deviceType.get('name'));
            }
        });
    },

    showDeviceConfigurationEditView: function(deviceTypeId,deviceConfigurationId){
        this.deviceTypeId=deviceTypeId;
        var me=this;
        var widget = Ext.widget('deviceConfigurationEdit', {
            edit: true,
            returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        var me = this;
        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);
        model.load(deviceConfigurationId, {
            success: function (deviceConfiguration) {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId,{
                    success: function(deviceType){
                        me.editBreadCrumb(deviceTypeId, deviceType.get('name'), deviceConfigurationId, deviceConfiguration.get('name'));
                        widget.down('form').loadRecord(deviceConfiguration);
                        widget.down('#deviceConfigurationEditCreateTitle').update('<H2>'+Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + deviceConfiguration.get('name')+'"</H2>');
                        widget.setLoading(false);
                    }
                });

            }
        });
    },

    createDeviceConfiguration: function(){
        var me=this;
        var record = Ext.create(Mdc.model.DeviceConfiguration),
            values = this.getDeviceConfigurationEditForm().getValues();
        if (record) {
            record.set(values);
            record.getProxy().setExtraParam('deviceType', this.deviceTypeId);
            record.save({
                success: function (record) {
                    location.href = '#setup/devicetypes/' + me.deviceTypeId + /deviceconfigurations/ + record.get('id');
                },
                failure: function(record,operation){
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceConfigurationEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    editDeviceConfiguration: function(){
        var record = this.getDeviceConfigurationEditForm().getRecord(),
            values = this.getDeviceConfigurationEditForm().getValues();
        var me=this;
        if (record) {
            record.set(values);
            record.getProxy().setExtraParam('deviceType', this.deviceTypeId);
            record.save({
                callback: function (record) {
                    location.href = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
                }
            });
        }
    },

    overviewBreadCrumb: function (id,name) {
        var breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem',{
            text: name,
            href: id
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild).setChild(breadcrumbChild2).setChild(breadcrumbChild3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumb: function (id,name) {
        var breadcrumbChild4 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
            href: 'create'
        });
        var breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem',{
            text: name,
            href: id
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild).setChild(breadcrumbChild2).setChild(breadcrumbChild3).setChild(breadcrumbChild4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    editBreadCrumb: function (deviceTypeId,deviceTypeName,deviceConfigurationId,deviceConfigurationName) {
        var breadcrumbChild5 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            href: 'edit'
        });
        var breadcrumbChild4 = Ext.create('Uni.model.BreadcrumbItem',{
            text: deviceConfigurationName,
            href: deviceConfigurationId
        });
        var breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem',{
            text: deviceTypeName,
            href: deviceTypeId
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild).setChild(breadcrumbChild2).setChild(breadcrumbChild3).setChild(breadcrumbChild4).setChild(breadcrumbChild5);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    detailBreadCrumb: function (deviceTypeId,deviceTypeName,deviceConfigurationId,deviceConfigurationName) {
        var breadcrumbChild5 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '"' + deviceConfigurationName + '"',
            href: 'overview'
        });
        var breadcrumbChild4 = Ext.create('Uni.model.BreadcrumbItem',{
            text: deviceConfigurationName,
            href: deviceConfigurationId
        });
        var breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem',{
            text: deviceTypeName,
            href: deviceTypeId
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild).setChild(breadcrumbChild2).setChild(breadcrumbChild3).setChild(breadcrumbChild4).setChild(breadcrumbChild5);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    }
});

