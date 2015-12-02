Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.time-sliced-custom-attribute-set-action-menu',
    itemId: 'time-sliced-custom-attribute-set-action-menu-id',
    type: null,
    plain: true,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-edit-btn-id',
                handler: function() {
                    me.fireEvent('moveToEditPage', me.type, me.record.get('versionId'));
                }
            },
            {
                text: Uni.I18n.translate('general.clone', 'MDC', 'Clone'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-clone-btn-id',
                handler: function() {
                    me.fireEvent('moveToClonePage', me.type, me.record.get('versionId'));
                }
            }
        ];

        me.callParent(arguments);
    }
});