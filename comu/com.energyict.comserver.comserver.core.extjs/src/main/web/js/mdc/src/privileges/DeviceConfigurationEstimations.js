/**
 * @class Mdc.privileges.DeviceConfigurationEstimations
 *
 * Class that defines privileges for Estimation rule sets on device configurations
 */
Ext.define('Mdc.privileges.DeviceConfigurationEstimations', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.view.fineTuneEstimationConfiguration.onDeviceConfiguration'],
    all: function() {
        return Mdc.privileges.DeviceConfigurationEstimations.view;
    }
});
