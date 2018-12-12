/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    view : ['privilege.view.EstimationConfiguration'],
    viewfineTuneEstimationConfiguration : ['privilege.view.fineTuneEstimationConfiguration.onDeviceConfiguration'],
    viewfineTuneEstimationConfigurationOnDevice : ['privilege.view.fineTuneEstimationConfiguration.onDevice'],
    administrate: ['privilege.administrate.EstimationConfiguration'],
    all: function() {
        return Ext.Array.merge(
            Mdc.privileges.DeviceConfigurationEstimations.view,
            Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfiguration,
            Mdc.privileges.DeviceConfigurationEstimations.administrate
        );
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.DeviceConfigurationEstimations.administrate);
    }
});
