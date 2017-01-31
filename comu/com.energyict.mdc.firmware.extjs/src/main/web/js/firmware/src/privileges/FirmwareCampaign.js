/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Fwc.privileges.FirmwareCampaign
 *
 * Class that defines privileges for FirmwareCampaign
 */
Ext.define('Fwc.privileges.FirmwareCampaign', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.firmware.campaign'],
    administrate: ['privilege.administrate.firmware.campaign'],
    all: function () {
        return Ext.Array.merge(
            Fwc.privileges.FirmwareCampaign.view,
            Fwc.privileges.FirmwareCampaign.administrate
        );
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Fwc.privileges.FirmwareCampaign.view);
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Fwc.privileges.FirmwareCampaign.administrate);
    }
});