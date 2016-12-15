Ext.define('Imt.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.time-sliced-custom-attribute-set-action-menu',
    type: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-edit-btn-id',
                privileges: Imt.privileges.UsagePoint.admin,
                handler: function() {
                    me.fireEvent('moveToEditPage', me.type, me.record.get('versionId'));
                },
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                itemId: 'time-sliced-custom-attribute-set-action-menu-clone-btn-id',
                privileges: Imt.privileges.UsagePoint.adminTimeSlicedCps,
                handler: function() {
                    me.fireEvent('moveToClonePage', me.type, me.record.get('versionId'));
                },
                section: this.SECTION_ACTION
            }
        ];

        me.callParent(arguments);
    }
});