Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.metrology-configuration-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'IMT', 'Activate'),
                action: 'activateMetrologyConfiguration',
                itemId: 'activate-metrology-config',
                section: this.SECTION_ACTION
            },

            {
                text: Uni.I18n.translate('general.deprecate', 'IMT', 'Deprecate'),
                action: 'deprecateMetrologyConfiguration',
                itemId: 'deprecate-metrology-config',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                action: 'removeMetrologyConfiguration',
                itemId: 'removeMetrologyConfiguration',
                section: this.SECTION_REMOVE,
                hidden: true // out of scope CXO-1209
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this,
                status = me.record.get('status').id.toLowerCase();

            Ext.suspendLayouts();
            me.down('#deprecate-metrology-config').setVisible(status === 'active');
            me.down('#activate-metrology-config').setVisible(status === 'inactive');
            Ext.resumeLayouts(true);
        }
    }
});
