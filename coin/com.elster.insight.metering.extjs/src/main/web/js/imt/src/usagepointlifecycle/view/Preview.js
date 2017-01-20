Ext.define('Imt.usagepointlifecycle.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usagepoint-life-cycles-preview',
    xtype: 'usagepoint-life-cycles-preview',

    requires: [
        'Imt.usagepointlifecycle.view.PreviewForm',
        'Imt.usagepointlifecycle.view.ActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Imt.privileges.UsagePointLifeCycle.configure,
            menu: {
                xtype: 'usagepoint-life-cycles-action-menu',
                itemId: 'lifeCyclesActionMenu'
            }
        }
    ],

    items: {
        xtype: 'usagepoint-life-cycles-preview-form',
        itemId: 'usagepoint-life-cycles-preview-form',
        isOverview: false
    }
});
