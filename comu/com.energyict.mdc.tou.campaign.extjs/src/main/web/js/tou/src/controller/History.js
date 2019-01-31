/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.History', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'TOU', 'Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                toucampaigns: {
                    title: Uni.I18n.translate('tou.campaigns.touCampaigns', 'TOU', 'ToU campaigns'),
                    route: 'toucampaigns',
                    controller: 'Tou.controller.Overview',
                    action: 'showOverview',
                    privileges: Tou.privileges.TouCampaign.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('tou.campaigns.addTouCampaign', 'TOU', 'Add ToU campaign'),
                            route: 'add',
                            controller: 'Tou.controller.Add',
                            action: 'showAdd',
                            privileges: Tou.privileges.TouCampaign.administrate
                        },
                        toucampaign: {
                            title: Uni.I18n.translate('tou.campaigns.touCampaign', 'TOU', 'ToU campaign'),
                            route: '{touCampaignName}',
                            controller: 'Tou.controller.Detail',
                            action: 'showDetail',
                            privileges: Tou.privileges.TouCampaign.view,
                            callback: function (route) {
                                this.getApplication().on('loadTouCampaign', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {
                                    single: true
                                });
                                return this;
                            },
                            items: {
                                devices: {
                                    title: Uni.I18n.translate('general.devices', 'TOU', 'Devices'),
                                    route: 'devices',
                                    controller: 'Tou.controller.Devices',
                                    action: 'showDevices',
                                    privileges: Tou.privileges.TouCampaign.view
                                },
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'TOU', 'Edit'),
                                    route: 'edit',
                                    controller: 'Tou.controller.Overview',
                                    action: 'editCampaign',
                                    privileges: Tou.privileges.TouCampaign.administrate
                                },
                            }
                        }
                    }
                }
            }
        }
    }
});