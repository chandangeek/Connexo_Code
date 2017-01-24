Ext.define('Mdc.filemanagement.view.SpecificationsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.files-spec-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'edit-files-specifications',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.DeviceType.admin,
                action: 'editspecifications',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ? item.show() : item.hide(); //hier nog privileges in de check?
                }
            })
        }
    }
});