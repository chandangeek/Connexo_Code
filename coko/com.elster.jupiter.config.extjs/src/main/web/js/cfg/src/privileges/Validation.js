/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Cfg.privileges.Validation
 *
 * Class that defines privileges for validation
 */
Ext.define('Cfg.privileges.Validation', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
        'privilege.view.fineTuneValidationConfiguration.onDevice', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
    viewTasks: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
    admin: ['privilege.administrate.validationConfiguration'],
    fineTuneValidation : ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration'],
    fineTuneOnDevice : ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration.onDevice'],
    fineTuneOnDeviceConfiguration: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
    device:['privilege.view.fineTuneValidationConfiguration.onDevice'],
    deviceConfiguration: ['privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
    viewOrAdmin:['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
    viewDataQuality:['privilege.view.dataQualityKpi'],
    administerDataQuality: ['privilege.administer.dataQualityKpi'],
    viewOrAdministerDataQuality: ['privilege.view.dataQualityKpi', 'privilege.administer.dataQualityKpi'],
    viewResultsOrAdministerDataQuality:['privilege.view.dataQualityResults', 'privilege.administer.dataQualityKpi'],
    validateManual: ['privilege.view.validateManual'],
    all: function() {
        return Ext.Array.merge(Cfg.privileges.Validation.view,
            Cfg.privileges.Validation.viewTasks,
            Cfg.privileges.Validation.device,
            Cfg.privileges.Validation.deviceConfiguration,
            Cfg.privileges.Validation.admin,
            Cfg.privileges.Validation.validateManual);
    },
    viewZones: ['privilege.view.zone', 'privilege.administrate.zone'],
    adminZones: ['privilege.administrate.zone'],
    canRun:function(){
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.validateManual);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.view);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.admin);
    },
    canViewOrAdministrate:function(){
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.viewOrAdmin);
    },
    canUpdateDeviceValidation : function (){
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.fineTuneOnDevice);
    },
    canUpdateDeviceConfigurationValidation : function (){
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.fineTuneOnDeviceConfiguration);
    },
    canViewOrAdministerDataQuality: function () {
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.viewOrAdministerDataQuality);
    },
    canAdministerDataQuality: function () {
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.administerDataQuality);
    },
    canViewResultsOrAdministerDataQuality: function () {
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.viewResultsOrAdministerDataQuality);
    },
    canViewZones: function() {
        return Uni.Auth.checkPrivileges(Cfg.privileges.Validation.viewZones);
    }
});
