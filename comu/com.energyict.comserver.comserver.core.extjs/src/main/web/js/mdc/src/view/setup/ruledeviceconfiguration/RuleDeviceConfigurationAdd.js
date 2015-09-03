Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-device-configuration-add',
    ruleSetId: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid'
    ],
    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('validation.deviceConfiguration.addMultiple', 'MDC', 'Add device configurations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'rule-device-configuration-add-grid',
                            ruleSetId: this.ruleSetId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('validation.empty.deviceConfiguration.title', 'MDC', 'No device configurations found'),
                            reasons: [
                                Uni.I18n.translate('deviceConfiguration.empty.list.item1', 'MDC', 'No device configurations have been added yet.'),
                                Uni.I18n.translate('deviceConfiguration.empty.list.item2', 'MDC', 'Device configurations exists, but you do not have permission to view them.')
                            ]
                        },
                        onLoad: function () {
                            var me = this,
                                count = me.grid.store.getCount(),
                                isEmpty = count === 0;

                            me.getLayout().setActiveItem(isEmpty ? 0 : 1);
                        }
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});