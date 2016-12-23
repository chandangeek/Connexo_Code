Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposeActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-action-menu',
    plain: true,
    border: false,
    shadow: false,

    listeners: {
        beforeshow: {
            fn: function () {
                var me = this,
                    isActive = me.record.get('active');

                Ext.suspendLayouts();
                me.removeAll();
                me.add({
                    itemId: 'purpose-trigger-activation',
                    text: isActive
                        ? Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate')
                        : Uni.I18n.translate('general.activate', 'IMT', 'Activate'),
                    action: 'triggerActivation'
                });
                Ext.resumeLayouts(true);
            }
        }
    }
});