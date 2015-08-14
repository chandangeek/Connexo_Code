Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-configuration-estimation-rule-sets-grid',
    store: 'Mdc.deviceconfigurationestimationrules.store.EstimationRuleSets',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.deviceconfigurationestimationrules.view.RuleSetActionMenu'
    ],

    initComponent: function () {
        var me = this,
            isOrder = me.router.queryParams.editOrder,
            buttons;


        me.columns = [
            {
                header: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.order', 'MDC', 'Order'),
                dataIndex: 'order',
                renderer: function (value, meta, record) {
                    return record.index + 1;
                }
            },
            {
                header: Uni.I18n.translate('general.estimationruleset', 'MDC', 'Estimation rule set'),
                dataIndex: 'name',
                flex: 2,
                renderer: function (value, meta, record) {
                    var res = '';
                    if (value && record && record.get('id')) {
                        var url = me.router.getRoute('administration/estimationrulesets/estimationruleset').buildUrl({ruleSetId: record.get('id')});
                        res = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                    return res;
                }
            },
            {
                header: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.activeRules', 'MDC', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.inactiveRules', 'MDC', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                flex: 2
            }
        ];

        if (isOrder) {
            me.viewConfig = {
                plugins: {
                    ptype: 'gridviewdragdrop',
                    dragText: '&nbsp;'
                },
                listeners: {
                    drop: {
                        fn: function () {
                            me.getView().refresh();
                        }
                    }
                }
            };
            me.selModel = {
                mode: 'MULTI'
            };
            me.columns.push({
                header: Uni.I18n.translate('general.ordering', 'MDC', 'Ordering'),
                flex: 1,
                renderer: function () {
                    return '<span class="icon-stack3"></span>';
                }
            });
            buttons = [
                {
                    xtype: 'button',
                    itemId: 'save-order-button',
                    text: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.saveorder', 'MDC', 'Save order'),
                    action: 'saveRuleSetsOrder',
                    privileges : Mdc.privileges.DeviceConfigurationEstimations.administrate
                },
                {
                    xtype: 'button',
                    itemId: 'undo-order-rulset-button',
                    text: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.orderundo', 'MDC', 'Undo'),
                    action: 'undoOrderingEstimationRuleSet',
                    privileges : Mdc.privileges.DeviceConfigurationEstimations.administrate
                }
            ]
        } else {
            me.columns.push(
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
                        xtype: 'device-configuration-estimation-rule-set-action-menu',
                        itemId: 'statesActionMenu'
                    },
                    privileges : Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfiguration
                });

            buttons = [
                {
                    xtype: 'button',
                    itemId: 'edit-order-button',
                    text: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.editorder', 'MDC', 'Edit order'),
                    action: 'editRuleSetsOrder',
                    privileges : Mdc.privileges.DeviceConfigurationEstimations.administrate
                },
                {
                    xtype: 'button',
                    itemId: 'add-rulset-button',
                    text: Uni.I18n.translate('estimationRuleSet.add', 'MDC', 'Add estimation rule sets'),
                    action: 'addEstimationRuleSet',
                    privileges : Mdc.privileges.DeviceConfigurationEstimations.administrate
                }
            ]
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                usesExactCount: true,
                displayMsg: Uni.I18n.translate('estimationrulesets.pagingtoolbartop.displayMsg', 'MDC', '{2} estimation rule sets'),
                emptyMsg: Uni.I18n.translate('estimationrulesets.pagingtoolbartop.emptyMsg', 'MDC', 'There are no estimation rule sets to display'),
                items: buttons
            }
        ];

        me.callParent(arguments);
    }
});

