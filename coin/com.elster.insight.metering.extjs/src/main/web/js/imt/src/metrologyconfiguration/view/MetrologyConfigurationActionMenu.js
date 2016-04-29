Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-action-menu',
    plain: true,
    border: false,
    shadow: false,

    items: [{}], // workaround of rendering menu for button

    listeners: {
        beforeshow: function () {
            var me = this,
                removeItem = {
                    text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                    action: 'removeMetrologyConfiguration',
                    itemId: 'removeMetrologyConfiguration',
                    hidden: true // out of scope CXO-1209
                };

            Ext.suspendLayouts();
            me.removeAll();
            switch (me.record.get('status').id.toLowerCase()) {
                case 'inactive':
                    me.add([
                        {
                            text: Uni.I18n.translate('general.activate', 'IMT', 'Activate'),
                            action: 'activateMetrologyConfiguration',
                            itemId: 'activate-metrology-config'
                        },
                        {
                            text: Uni.I18n.translate('metrologyConfigPurposes.add', 'IMT', 'Add purpose'),
                            action: 'addPurposeMetrologyConfiguration',
                            itemId: 'add-metrology-config-purpose',
                            disabled: true // out of scope CXO-1209
                        },
                        {
                            text: Uni.I18n.translate('general.menu.editGeneralInformation', 'IMT', 'Edit general information'),
                            action: 'editMetrologyConfiguration',
                            itemId: 'edit-metrology-config-general-info',
                            disabled: true // out of scope CXO-1209
                        },
                        removeItem
                    ]);
                    break;
                case 'active':
                    me.add([
                        {
                            text: Uni.I18n.translate('general.deprecate', 'IMT', 'Deprecate'),
                            action: 'deprecateMetrologyConfiguration',
                            itemId: 'deprecate-metrology-config',
                            disabled: true // out of scope CXO-1209
                        }
                    ]);
                    break;
                case 'deprecated':
                    me.add(removeItem);
                    break;
            }
            Ext.resumeLayouts(true);
        }
    }
});
