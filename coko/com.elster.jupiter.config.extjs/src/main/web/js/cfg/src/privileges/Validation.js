/**
 * @class Cfg.privileges.Validation
 *
 * Class that defines privileges for validation
 */
Ext.define('Cfg.privileges.Validation', {
    singleton: true,
    view : ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
        'privilege.view.fineTuneValidationConfiguration.onDevice', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
    viewTasks : ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
    device:['privilege.view.fineTuneValidationConfiguration.onDevice'],
    deviceConfiguration: ['privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
    admin: ['privilege.administrate.validationConfiguration']
});
