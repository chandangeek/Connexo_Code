Ext.define('Imt.usagepointlifecycle.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usagepoint-life-cycles-action-menu',
    xtype: 'usagepoint-life-cycles-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
            action: 'clone'
        },
        {
            text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
            action: 'edit'
        },
        {
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
            action: 'remove'
        },        
        {
            itemId: 'set-as-default',
            text: Uni.I18n.translate('general.setAsDefault', 'IMT', 'Set as default'),
            action: 'setAsDefault'
        }
    ]
});