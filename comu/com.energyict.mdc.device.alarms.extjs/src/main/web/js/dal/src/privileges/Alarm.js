Ext.define('Dal.privileges.Alarm', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceCommunication: ['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'],
    viewLogbook: ['Mdc.privileges.MasterData.view'],
    viewUsagePoint: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    viewAdminProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewAdminDevice: ['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'],
    adminDevice: ['privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'],

    all: function () {
        return Ext.Array.merge();

    },

    canViewDeviceCommunication: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.viewDeviceCommunication);
    },
    canViewLogbook: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.viewLogbook);
    },
    canViewUsagePoint: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.viewUsagePoint);
    },
    canViewProcesses: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.viewAdminProcesses);
    }


});
