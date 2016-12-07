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
                }
            ];
        }

        me.callParent(arguments);
    }
});