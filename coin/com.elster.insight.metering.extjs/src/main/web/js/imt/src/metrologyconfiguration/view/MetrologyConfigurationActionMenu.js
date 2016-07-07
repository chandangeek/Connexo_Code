Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-action-menu',
    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.activate', 'IMT', 'Activate'),
            action: 'activateMetrologyConfiguration',
            itemId: 'activate-metrology-config'
        },

        {
            text: Uni.I18n.translate('general.deprecate', 'IMT', 'Deprecate'),
            action: 'deprecateMetrologyConfiguration',
            itemId: 'deprecate-metrology-config'
        },
        {
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
            action: 'removeMetrologyConfiguration',
            itemId: 'removeMetrologyConfiguration',
            hidden: true // out of scope CXO-1209
        }
    ],

    listeners: {
        beforeshow: function () {
            var me = this,
                status = me.record.get('status').id.toLowerCase();

            Ext.suspendLayouts();
            me.down('#deprecate-metrology-config').setVisible(status == 'active');
            me.down('#activate-metrology-config').setVisible(status == 'inactive');
            Ext.resumeLayouts(true);
        }
    }
});
