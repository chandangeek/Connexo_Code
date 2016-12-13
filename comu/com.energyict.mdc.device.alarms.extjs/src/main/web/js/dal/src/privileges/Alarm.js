Ext.define('Dal.privileges.Alarm', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceCommunication: ['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'],
    viewLogbook: ['Mdc.privileges.MasterData.view'],
    viewUsagePoint: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    viewAdminProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewAdminDevice: ['privilege.view.alarm', 'privilege.comment.issalarmue', 'privilege.close.alarm', 'privilege.assign.alarm', 'privilege.action.alarm'],
    adminDevice: ['privilege.comment.alarm', 'privilege.close.alarm', 'privilege.assign.alarm', 'privilege.action.alarm'],
    comment: ['privilege.comment.alarm'],

    all: function () {
        return Ext.Array.merge(Dal.privileges.Alarm.viewDeviceCommunication,
            Dal.privileges.Alarm.viewLogbook,
            Dal.privileges.Alarm.viewUsagePoint,
            Dal.privileges.Alarm.viewAdminProcesses,
            Dal.privileges.Alarm.viewAdminDevice,
            Dal.privileges.Alarm.adminDevice,
            Dal.privileges.Alarm.comment);
    },

    canViewAdminDevice: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminDevice);
    },
    canViewDeviceCommunication: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewDeviceCommunication);
    },
    canViewLogbook: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewLogbook);
    },
    canViewUsagePoint: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewUsagePoint);
    },
    canViewProcesses: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminProcesses);
    },
    canComment: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.comment);
    },


});
