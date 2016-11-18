Ext.define('Imt.usagepointlifecycletransitions.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usagepoint-life-cycle-transitions-preview',

    requires: [
        'Imt.usagepointlifecycletransitions.view.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Imt.privileges.UsagePointLifeCycle.configure,
            menu: {
                xtype: 'transitions-action-menu',
                itemId: 'transitions-action-menu'
            }
        }
    ],

    items: {
        xtype: 'usagepoint-life-cycle-transitions-preview-form',
        itemId: 'usagepoint-life-cycle-transitions-preview-form'
    }
});
