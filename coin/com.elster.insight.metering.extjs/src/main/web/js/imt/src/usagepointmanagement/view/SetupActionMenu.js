Ext.define('Imt.usagepointmanagement.view.SetupActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usage-point-setup-action-menu',
    plain: true,
    border: false,
    shadow: false,
    router: null,

    initComponent: function() {
        var me = this;

        if (Dbp.privileges.DeviceProcesses.canAssignOrExecute()) {
            me.items = [
                {
                    itemId: 'action-menu-item-start-proc',
                    privileges: Dbp.privileges.DeviceProcesses.assignOrExecute,
                    text: Uni.I18n.translate('usagepoint.process.startProcess', 'IMT', 'Start process'),
                    href: me.router.getRoute('usagepoints/view/processstart').buildUrl()
                }
            ];
        }

        me.callParent(arguments);
    },

    setActions: function(actionsStore, router) {
        var me = this;

        actionsStore.each(function(item) {
            me.add({
                itemId: 'action-menu-item' + item.get('id'),
                text: item.get('name'),
                handler: function() {
                    router.getRoute('usagepoints/view/transitions').forward({transitionId: item.get('id')});
                }
            })
        });
    }
});