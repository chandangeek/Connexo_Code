Ext.define('Mdc.view.setup.deviceloadprofiles.DataActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceLoadProfilesDataActionMenu',
    itemId: 'deviceLoadProfilesDataActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'viewHistory',
                text: Uni.I18n.translate('deviceloadprofiles.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});
