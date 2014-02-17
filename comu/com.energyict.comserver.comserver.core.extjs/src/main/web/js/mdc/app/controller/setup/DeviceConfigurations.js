Ext.define('Mdc.controller.setup.DeviceConfigurations', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'setup.deviceconfiguration.DeviceConfigurationsSetup'
    ],

    stores: [

    ],

    refs: [

    ],

    init: function () {
        this.control({

        });
    },

    showEditView: function (id) {

    },

    showDeviceConfigurations: function (id) {
//        var me = this;
//        this.getRegisterMappingsStore().getProxy().setExtraParam('deviceType', id);
        var widget = Ext.widget('deviceConfigurationsSetup');//, {deviceTypeId: id});
//        //this.getRegisterMappingGrid().setDeviceType(id);
//        this.getAddRegisterMappingBtn().href = '#/setup/devicetypes/' + id + '/registermappings/add';
//        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
//            success: function (deviceType) {
//                var deviceTypeName = deviceType.get('name');
//                widget.down('#registerTypeTitle').html = '<h1>' + deviceTypeName + ' > ' + 'Register types' + '</h1>';
                Mdc.getApplication().getMainController().showContent(widget);
//                me.createBreadCrumbs(id, deviceTypeName);
//            }
//        });
    }
});

