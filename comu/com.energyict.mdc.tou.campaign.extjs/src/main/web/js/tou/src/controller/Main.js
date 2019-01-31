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
        'Tou.controller.Devices',
        'Tou.controller.Overview',
        'Tou.controller.Add'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
          historian = me.getController('Tou.controller.History'); // Forces route registration.
          if (Tou.privileges.TouCampaign.canView()) {
              var portalItemLink = {
                            text: Uni.I18n.translate('tou.campaigns.touCampaigns', 'TOU', 'ToU campaigns'),
                            itemId:'tou-campaigns-link-tou',
                            href: '#/workspace/toucampaigns',
                            route: 'workspace',
                            privileges: Tou.privileges.TouCampaign.view
                        };
              var portalItem = Uni.store.PortalItems.findRecord('id', 'Campaigns');
              if (portalItem){
                  var portalItemData = portalItem.get('items');
                  portalItemData.push(portalItemLink);
              }else{
                  Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                                      title: Uni.I18n.translate('tou.touManagement', 'TOU', 'ToU campaigns'),
                                      portal: 'workspace',
                                      route: 'toucampaigns',
                                      items: [portalItemLink]
                                      }));
              }
          }
       // me.getApplication().fireEvent('cfginitialized');
    }
});