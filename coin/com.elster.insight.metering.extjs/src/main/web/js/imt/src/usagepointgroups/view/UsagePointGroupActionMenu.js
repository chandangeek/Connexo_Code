Ext.define('Imt.usagepointgroups.view.UsagePointGroupActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usagepointgroup-action-menu',
    xtype: 'usagepointgroup-action-menu',
    plain: true,
    border: false,    
    shadow: false,
    record: null,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),            
            itemId: 'edit-usagepointgroup',
            action: 'editUsagePointGroup'
        },
        {
            text: Uni.I18n.translate('general.usagePointGroup.remove', 'IMT', 'Remove'),
            itemId: 'remove-usagepointgroup',
            action: 'removeUsagePointGroup'
        }
    ]
});
