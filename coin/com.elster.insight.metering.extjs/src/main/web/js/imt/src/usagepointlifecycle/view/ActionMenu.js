Ext.define('Imt.usagepointlifecycle.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.usagepoint-life-cycles-action-menu',
    xtype: 'usagepoint-life-cycles-action-menu',
    initComponent: function () {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                itemId: 'clone-action',
                action: 'clone',
                section: me.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                itemId: 'edit-action',
                action: 'edit',
                section: me.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                itemId: 'remove-action',
                action: 'remove',
                section: me.SECTION_REMOVE
            },
            {
                itemId: 'set-as-default',
                text: Uni.I18n.translate('general.setAsDefault', 'IMT', 'Set as default'),
                action: 'setAsDefault',
                section: me.SECTION_ACTION
            }
        ];
        me.callParent();
    }
});