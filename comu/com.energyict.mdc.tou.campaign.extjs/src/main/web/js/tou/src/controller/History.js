/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.History', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: 'Tou campaigns',
            route: 'workspace',
            disabled: true,
            items: {
                toucampaigns: {
                    title: 'Tou campaigns',
                    route: 'toucampaigns',
                    controller: 'Tou.controller.Overview',
                    action: 'showOverview',
                    privileges: Fwc.privileges.FirmwareCampaign.view,
                    items: {
                        add: {
                            title: 'Add tou campaign',
                             route: 'add',
                             controller: 'Tou.controller.Add',
                             action: 'showAdd',
                             privileges: Fwc.privileges.FirmwareCampaign.administrate
                        },
                        toucampaign: {
                           title: 'Tou campaign',
                            route: '{touCampaignId}',
                            controller: 'Tou.controller.Detail',
                            action: 'showDetail',
                            privileges: Fwc.privileges.FirmwareCampaign.view,
                            callback: function (route) {
                                this.getApplication().on('loadTouCampaign', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                devices: {
                                    title: 'Devices',
                                    route: 'devices',
                                    controller: 'Fwc.firmwarecampaigns.controller.Devices',
                                    action: 'showDevices',
                                    privileges: Fwc.privileges.FirmwareCampaign.view
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});