/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Uni.property.view.property.SelectionGrid',
        'Bpm.controller.Task',
        'Bpm.controller.TaskBulk',
        'Bpm.privileges.BpmManagement',
        'Bpm.monitorprocesses.controller.MonitorProcesses',
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.processes.controller.Processes',
        'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses'
    ],

    controllers: [
        'Bpm.controller.history.BpmManagement',
        'Bpm.controller.Task',
        'Bpm.controller.TaskBulk',
        'Bpm.monitorprocesses.controller.MonitorProcesses',
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.processes.controller.Processes',
        'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    addProcessManagement: function () {
        var me = this,
            menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'BPM', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });

        Uni.store.MenuItems.add(menuItem);

        if (Bpm.privileges.BpmManagement.canViewProcesses()) {

            Ext.require(['Bpm.controller.history.BpmManagement'],
                function(){
                    me.getController('Bpm.controller.history.BpmManagement'); // Forces route registration.
                }
            );

            var processManagement = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.processManagement', 'BPM', 'Process management'),
                portal: 'administration',

                items: [
                    {
                        text: Uni.I18n.translate('general.managementprocesses.processes', 'BPM', 'Processes'),
                        itemId: 'processes',
                        href: '#/administration/managementprocesses'
                    }
                ]
            });
            Uni.store.PortalItems.add(processManagement);
        }
    },

    initNavigtaion: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    showContent: function (widget) {
        this.clearContentPanel();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    clearContentPanel: function () {
        this.getContentPanel().removeAll(false);
    }
});