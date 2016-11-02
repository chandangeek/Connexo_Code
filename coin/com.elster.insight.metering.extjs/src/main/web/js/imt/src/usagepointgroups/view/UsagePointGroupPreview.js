Ext.define('Imt.usagepointgroups.view.UsagePointGroupPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'usagepointgroup-preview',
    alias: 'widget.usagepointgroup-preview',
    frame: true,
    requires: [
        'Imt.usagepointgroups.view.UsagePointGroupActionMenu',
        'Imt.usagepointgroups.view.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'usagepointgroup-preview-actions-btn',
            menu: {
                xtype: 'usagepointgroup-action-menu',
                itemId: 'usagepointgroup-action-menu'
            }
        }
    ],

    items: {
        xtype: 'usagepointgroup-preview-form',
        itemId: 'usagepointgroup-preview-form'
    }
});
