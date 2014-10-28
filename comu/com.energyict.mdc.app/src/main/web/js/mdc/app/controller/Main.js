/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo MultiSense',
    defaultToken: '#/workspace',
    searchEnabled: Uni.Auth.hasAnyPrivilege(['privilege.administrate.device','privilege.view.device']),
    privileges: ['privilege.view.issue','privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue',
        'privilege.view.creationRule','privilege.administrate.creationRule','privilege.view.assignmentRule','privilege.administrate.validationConfiguration',
        'privilege.view.validationConfiguration','privilege.administrate.schedule','privilege.view.schedule','privilege.administrate.communicationInfrastructure',
        'privilege.view.communicationInfrastructure','privilege.administrate.protocol','privilege.view.protocol','privilege.administrate.deviceConfiguration',
        'privilege.view.deviceConfiguration','privilege.administrate.device','privilege.view.device','privilege.view.validateDevice','privilege.view.scheduleDevice',
        'privilege.import.inventoryManagement','privilege.revoke.inventoryManagement','privilege.create.inventoryManagement','privilege.administrate.deviceSecurity',
        'privilege.view.deviceSecurity'],
    controllers: [
        'Cfg.controller.Main',
        'Mdc.controller.Main',
        'Isu.controller.Main',
        'Idc.controller.Main',
        'Dvi.controller.Main',
        'Dsh.controller.Main'
    ]
});
