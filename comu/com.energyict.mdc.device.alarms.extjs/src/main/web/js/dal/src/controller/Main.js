/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Dal.privileges.Alarm'
    ],

    controllers: [
        'Dal.controller.history.Workspace',
        'Dal.controller.Alarms',
        'Dal.controller.Detail',
        'Dal.controller.ApplyAction',
        'Dal.controller.StartProcess',
        'Dal.controller.Overview',
        'Dal.controller.SetPriority',
        'Dal.controller.BulkChangeAlarms'
    ],

    stores: [
    ],

    refs: [],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            alarms = null,
            alarmManagement = null,
            historian = me.getController('Dal.controller.history.Workspace'); // Forces route registration.

        if (Dal.privileges.Alarm.canViewAdmimAlarm()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'DAL', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));
        }

        if (Dal.privileges.Alarm.canViewAdmimAlarm()) {
            alarms = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.alarms', 'DAL', 'Alarms'),
                portal: 'workspace',
                route: 'alarms',
                items: [
                    {
                        itemId: 'alarms-item',
                        text: Uni.I18n.translate('device.alarms', 'DAL', 'Alarms'),
                        href: router.getRoute('workspace/alarms').buildUrl({}, {
                            status: ['status.open', 'status.in.progress']
                        })
                    },
                    {
                        text: Uni.I18n.translate('device.alarms.alarmsOverview', 'DAL', 'Alarms overview'),
                        itemId: 'alarms-overview-item',
                        href: router.getRoute('workspace/alarmsoverview').buildUrl()
                    },
                    {
                        itemId: 'my-open-alarms-item',
                        text: Uni.I18n.translate('device.myOpenAlarms','DAL','My open alarms'),
                        href: router.getRoute('workspace/alarms').buildUrl({}, {
                            status: ['status.open', 'status.in.progress'],
                            myopenalarms: true
                        })
                    },
                    {
                        itemId: 'my-workgroup-alarms-item',
                        text: Uni.I18n.translate('device.myWorkgroupsAlarms', 'DAL', 'My workgroups alarms'),
                        href: router.getRoute('workspace/alarms').buildUrl({}, {
                            status: ['status.open', 'status.in.progress'],
                            myworkgroupalarms: true
                        })
                    }
                ]
            });
        }
        if (Dal.privileges.Alarm.canViewAdminAlarmCreationRule()){
            alarmManagement = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.alarm.managemnet', 'DAL', 'Alarm management'),
                portal: 'administration',
                route: 'alarmmanagement',
                items: [
                    {
                        text: Uni.I18n.translate('general.alarmCreationRules','DAL','Alarm creation rules'),
                        href: router.getRoute('administration/alarmcreationrules').buildUrl()
                    }
                ]
            });
        }

        if (alarms !== null) {
            Uni.store.PortalItems.add(alarms);
        }
        if (alarmManagement !== null){
            Uni.store.PortalItems.add(alarmManagement);
        }
    }
});