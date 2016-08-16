Ext.define('Dlc.devicelifecyclestates.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycle-states-preview',

    requires: [
        'Dlc.devicelifecyclestates.view.PreviewForm',
        'Dlc.devicelifecyclestates.view.ActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            dynamicPrivilege: Dlc.dynamicprivileges.DeviceLifeCycle.viable,
            menu: {
                xtype: 'device-life-cycle-states-action-menu',
                itemId: 'statesActionMenu'
            }
        }
    ],

    items: {
        xtype: 'device-life-cycle-states-preview-form',
        itemId: 'device-life-cycle-states-preview-form'
    }
});
