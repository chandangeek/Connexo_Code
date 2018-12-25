/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems',
        'Fwc.privileges.FirmwareCampaign',
    ],

    controllers: [
        'Tou.controller.History',
        //'Tou.controller.Overview'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this;
          historian = me.getController('Tou.controller.History'); // Forces route registration.
          Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: 'Tou campains',
                portal: 'workspace',
                route: 'toucampaigns',
                items: [
                    {
                        text: 'Tou campains',
                        itemId:'tou-campaigns-link-tou',
                        href: '#/workspace/toucampaigns',
                        route: 'workspace',
                        privileges: Fwc.privileges.FirmwareCampaign.view
                    }
                ]
          }));
       // me.getApplication().fireEvent('cfginitialized');
    }
});