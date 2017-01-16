Ext.define('Imt.purpose.view.MultipleReadingsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.purpose-bulk-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'confirm-value',
                privileges: Imt.privileges.UsagePoint.admin,
                hidden: true,
                text: Uni.I18n.translate('general.confirmValue', 'IMT', 'Confirm'),
                action: 'confirmValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value',
                privileges: Imt.privileges.UsagePoint.admin,
                hidden: true,
                text: Uni.I18n.translate('general.estimateValue', 'IMT', 'Estimate'),
                action: 'estimateValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'reset-value',
                privileges: Imt.privileges.UsagePoint.admin,
                hidden: true,
                text: Uni.I18n.translate('general.restoreReadings', 'IMT', 'Restore'),
                action: 'resetValue',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }

});
