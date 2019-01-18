/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems',
        'Tou.privileges.TouCampaign',
    ],

    controllers: [
        'Tou.controller.History',
        'Tou.controller.Detail',
        'Tou.controller.Devices'
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
                title: 'ToU campaigns',
                portal: 'workspace',
                route: 'toucampaigns',
                items: [
                    {
                        text: 'ToU campaigns',
                        itemId:'tou-campaigns-link-tou',
                        href: '#/workspace/toucampaigns',
                        route: 'workspace',
                        privileges: Tou.privileges.TouCampaign.view
                    }
                ]
          }));
       // me.getApplication().fireEvent('cfginitialized');
    }
});