Ext.define('Imt.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.time-sliced-custom-attribute-set-action-menu',
    type: null,
    plain: true,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-edit-btn-id',
                privileges: Imt.privileges.UsagePoint.admin,
                handler: function() {
                    me.fireEvent('moveToEditPage', me.type, me.record.get('versionId'));
                }
            },
            {
                text: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-clone-btn-id',
                privileges: Imt.privileges.UsagePoint.adminTimeSlicedCps,
                handler: function() {
                    me.fireEvent('moveToClonePage', me.type, me.record.get('versionId'));
                }
            }
        ];

        me.callParent(arguments);
    }
});