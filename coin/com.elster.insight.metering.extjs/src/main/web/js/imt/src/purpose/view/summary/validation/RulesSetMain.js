/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RulesSetMain', {
    extend: 'Ext.container.Container',
    alias: 'widget.validationConfigurationRulesSetMain',
    itemId: 'validationConfigurationRulesSetMain',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'panel',
                margin: '0 0 0 -10',
                itemId: 'validationConfigurationStatusPanel',
                ui: 'medium',
                title: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                layout: 'fit',
                items: {
                    xtype: 'toolbar',
                    items: [
                        {
                            xtype: 'displayfield',
                            itemId: 'validationConfigurationStatusField',
                            columnWidth: 1,
                            labelAlign: 'left',
                            fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                            htmlEncode: false,
                            valueToRaw: function (v) {
                                return v;
                            },
                            renderer: function (value) {
                                var status = 'Updating status ...',
                                    icon = '';

                                switch (value) {
                                    case true:
                                        status = Uni.I18n.translate('purpose.validation.status.active', 'IMT', 'Active');
                                        icon = '<span class="icon-checkmark-circle" style="color: #33CC33; margin-left: 10px"></span>';
                                        break;
                                    case false:
                                        status = Uni.I18n.translate('purpose.validation.status.inactive', 'IMT', 'Inactive');
                                        icon = '<span class="icon-blocked" style="color: #eb5642; margin-left: 10px"></span>';
                                        break;
                                }
                                return status + icon
                            }
                        },
                        '->',
                        {
                            xtype: 'button',
                            itemId: 'validationConfigurationStateChangeBtn',
                            disabled: false,
                            // privileges: Cfg.privileges.Validation.device,
                            action: '',
                            //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions
                        }
                    ]
                }

            },
            {
                xtype: 'validationConfigurationRulesSetMainView',
                itemId: 'validationConfigurationRulesSetMainView',
                device: me.device,
                purpose: me.purpose,
                usagePoint: me.usagePoint,
                router: me.router,
                outputs: me.outputs,
                prevNextListLink: me.prevNextListLink

            }
        ];
        me.callParent(arguments);
    }
});