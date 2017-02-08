/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.validation.PurposeWithRuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purpose-with-rule-sets-grid',
    purposes: null,
    router: null,
    metrologyConfig: null,
    requires: [
        'Uni.grid.column.RemoveAction'
    ],

    initComponent: function () {
        var me = this;

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

                        ruleSetsCount = Uni.I18n.translatePlural('validationRuleSets.count', count, 'IMT',
                            'No validation rule sets',
                            '{0} validation rule set',
                            '{0} validation rule sets');

                        return record.get('metrologyContractIsMandatory') ? '<span class="uni-form-item-label-required" style="display: inline-block; width: 16px; height: 16px;"></span> (' + ruleSetsCount + ')' : '(' + ruleSetsCount + ')';
                    }
                }
            ]
        }];

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return '<a href="' + me.router.getRoute('administration/rulesets/overview').buildUrl({ruleSetId: record.get('id')}) + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'currentVersion',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn-remove',
                itemId: 'remove-rule-set-from-purpose-column',
                hidden: me.metrologyConfig.get('status').id == 'deprecated',
                privileges: Imt.privileges.MetrologyConfig.adminValidation,
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    this.fireEvent('removeRuleSetFromPurpose', record);
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'purpose-rule-sets-grid-pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('metrologyConfiguration.validation.ruleSetsCount', 'IMT', '{0} validation rule set(s)', me.getStore().totalCount),
                noBottomPaging: true,
                usesExactCount: true,
                items: [
                    {
                        xtype: 'button',
                        itemId: 'metrology-config-add-validation-rule-set-btn',
                        text: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
                        privileges: Imt.privileges.MetrologyConfig.adminValidation,
                        action: 'addValidationRuleSet',
                        href: me.router.getRoute('administration/metrologyconfiguration/view/validation/add').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
