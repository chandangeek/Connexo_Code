Ext.define('Mdc.controller.setup.ValidationRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'setup.devicetype.validation.RulesOverview'
    ],

    stores: [
        // TODO
//        'ValidationRules',
//        'ValidationRuleSets'
    ],

    refs: [
        // TODO
    ],

    deviceTypeId: null,
    deviceConfigurationId: null,

    init: function () {
        // TODO
    },

    showValidationRulesOverview: function (deviceTypeId, deviceConfigurationId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;

        var widget = Ext.widget('validation-rules-overview', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getValidationRuleSetsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    }
});
