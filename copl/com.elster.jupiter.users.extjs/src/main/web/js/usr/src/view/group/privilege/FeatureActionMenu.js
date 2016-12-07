Ext.define('Usr.view.group.privilege.FeatureActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.feature-action-menu',
    itemId: 'feature-action-menu',
    initComponent: function() {
        this.items = [];
        this.callParent(arguments);
    }
});
