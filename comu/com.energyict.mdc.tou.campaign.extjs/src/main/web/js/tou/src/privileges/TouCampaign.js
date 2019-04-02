/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Tou.privileges.TouCampaign
 *
 * Class that defines privileges for TouCampaign
 */
Ext.define('Tou.privileges.TouCampaign', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.touCampaigns', 'privilege.administer.touCampaigns'],
    administrate: ['privilege.administer.touCampaigns'],
    all: function () {
        return Ext.Array.merge(
            Tou.privileges.TouCampaign.view,
            Tou.privileges.TouCampaign.administrate);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Tou.privileges.TouCampaign.view);
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Tou.privileges.TouCampaign.administrate);
    }
});