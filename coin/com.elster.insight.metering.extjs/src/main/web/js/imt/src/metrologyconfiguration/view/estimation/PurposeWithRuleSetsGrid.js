Ext.define('Imt.metrologyconfiguration.view.estimation.PurposeWithRuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.est-purpose-with-rule-sets-grid',
    purposes: null,
    router: null,
    metrologyConfig: null,
    requires: [
        'Uni.grid.column.RemoveAction',
        'Uni.grid.plugin.DragDropForGridWithGroups'
    ],

    initComponent: function () {
        var me = this,
            buttons,
            rulesetsCounter = 0,
            currentMetrologyContractId = 0;


        me.features = [{
            ftype: 'grouping',
            groupHeaderTpl: [
                '{name} {name:this.createHeader}',
                {
                    createHeader: function (name) {
                        var store = me.getStore(),
                            record = store.getAt(store.find('metrologyContract', name)),
                            count = 0,
                            ruleSetsCount;

                        if (!record.get('noRuleSets')) {
                            store.each(function (record) {
                                if (record.get('metrologyContract') == name) {
                                    count++;
                                }
                            });
                        } else {

                        }

                        ruleSetsCount = Uni.I18n.translatePlural('estimationRuleSets.count', count, 'IMT',
                            'No estimation rule sets',
                            '{0} estimation rule set',
                            '{0} estimation rule sets');

                        return record.get('metrologyContractIsMandatory') ? '<span class="uni-form-item-label-required" style="display: inline-block; width: 16px; height: 16px;"></span> (' + ruleSetsCount + ')' : '(' + ruleSetsCount + ')';
                    }
                }
            ]
        }];

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return '<a href="' + me.router.getRoute('administration/estimationrulesets/estimationruleset').buildUrl({ruleSetId: record.get('id')}) + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.label.activeRules', 'IMT', 'Active rules'),
                dataIndex: 'activeRules',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.label.inactiveRules', 'IMT', 'Inactive rules'),
                dataIndex: 'inactiveRules',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.order', 'IMT', 'Order'),
                renderer: function (value, metaData, record, rowIndex) {
                    if(currentMetrologyContractId != record.get('metrologyContractId')){
                        rulesetsCounter = rowIndex;
                        currentMetrologyContractId = record.get('metrologyContractId');
                    }
                    return ++rowIndex - rulesetsCounter;
                }
            }
        ];

        if (me.editOrder) {
            me.viewConfig = {
                plugins: {
                    ptype: 'gridviewwithgroupsdragdrop',
                    groupByHiddenField: 'hiddenGroupId',
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
            me.columns.push(
                {
                    header: Uni.I18n.translate('general.ordering', 'IMT', 'Ordering'),
                    align: 'center',
                    renderer: function () {
                        return '<span class="icon-stack3"></span>';
                    }
                },
                //Hidden column is needed for gridviewwithgroupsdragdrop plugin (hidden: true will remove this column from html code)
                {
                    dataIndex: 'hiddenGroupId',
                    width: 1
                }
            );

            buttons = [
                {
                    xtype: 'button',
                    itemId: 'btn-save-estimation-rules-order',
                    text: Uni.I18n.translate('general.saveOrder', 'IMT', 'Save order'),
                    action: 'saveEstimationRuleSetsOrder',
                    privileges: Imt.privileges.MetrologyConfig.adminEstimation
                },
                {
                    xtype: 'button',
                    itemId: 'btn-undo-estimation-rules-order',
                    text: Uni.I18n.translate('general.undo', 'IMT', 'Undo'),
                    action: 'undoEstimationRuleSetsOrder',
                    href: me.router.getRoute(me.router.currentRoute).buildUrl(me.router.arguments, null),
                    privileges: Imt.privileges.MetrologyConfig.adminEstimation
                }
            ]
        } else {
            me.columns.push({
                xtype: 'uni-actioncolumn-remove',
                itemId: 'remove-rule-set-from-purpose-column',
                hidden: me.metrologyConfig.get('status').id == 'deprecated',
                privileges: Imt.privileges.MetrologyConfig.adminEstimation,
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    this.fireEvent('removeRuleSetFromPurpose', record);
                }
            });

            buttons = [
                {
                    xtype: 'button',
                    itemId: 'btn-edit-order-estimation-rules',
                    text: Uni.I18n.translate('estimation.general.editOrder', 'IMT', 'Edit order'),
                    action: 'editOrderEstimationRules',
                    href: me.router.getRoute('administration/metrologyconfiguration/view/estimation').buildUrl(me.router.arguments, {editOrder: true}),
                    privileges: Imt.privileges.MetrologyConfig.adminEstimation
                },
                {
                    xtype: 'button',
                    itemId: 'metrology-config-add-estimation-rule-set-btn',
                    text: Uni.I18n.translate('estimation.addRuleSet', 'IMT', 'Add estimation rule set'),
                    privileges: Imt.privileges.MetrologyConfig.adminEstimation,
                    action: 'addEstimationRuleSet',
                    href: me.router.getRoute('administration/metrologyconfiguration/view/estimation/add').buildUrl()
                }
            ]
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'purpose-rule-sets-grid-pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('metrologyConfiguration.estimation.ruleSetsCount', 'IMT', '{0} estimation rule set(s)', me.getStore().totalCount),
                noBottomPaging: true,
                usesExactCount: true,
                items: buttons
            }
        ];

        me.callParent(arguments);
    }
});
