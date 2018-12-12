/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.SetupActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usage-point-setup-action-menu',
    router: null,

    initComponent: function() {
        var me = this;

        if (Dbp.privileges.DeviceProcesses.canAssignOrExecute()) {
            me.items = [
                {
                    itemId: 'action-menu-item-start-proc',
                    privileges: Dbp.privileges.DeviceProcesses.assignOrExecute,
                    text: Uni.I18n.translate('usagepoint.process.startProcess', 'IMT', 'Start process'),
                    href: me.router.getRoute('usagepoints/view/processstart').buildUrl(),
                    section: this.SECTION_ACTION
                },
                {
                    itemId: 'action-menu-item-change-lifecycle',
                    privileges: Dbp.privileges.DeviceProcesses.assignOrExecute,
                    text: Uni.I18n.translate('usagepoint.process.change.lifecycle', 'IMT', 'Change usage point lifecycle '),
                    href: me.router.getRoute('usagepoints/view/change').buildUrl(),
                    section: this.SECTION_EDIT
                }

            ];
        }

        me.callParent(arguments);
    },

    setActions: function (actionsStore, router) {
        var me = this;

        actionsStore.each(function (item) {
            me.add({
                itemId: 'action-menu-item' + item.get('id'),
                text: item.get('name'),
                handler: function () {
                    router.getRoute('usagepoints/view/transitions').forward({transitionId: item.get('id')});
                },
                section: me.SECTION_ACTION
            })
        });
    }
});