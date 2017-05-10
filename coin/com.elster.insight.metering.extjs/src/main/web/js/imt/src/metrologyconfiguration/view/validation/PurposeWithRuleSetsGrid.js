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
                header: Uni.I18n.translate('validation.usagePointStates', 'IMT', 'Usage point states'),
                dataIndex: 'lifeCycleStatesCount',
                flex: 1,
                renderer: me.statesColumnRenderer
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

        me.addListener('cellclick', function(view, td, cellIndex, record, tr, rowIndex, e, eOpts){
            if(cellIndex == 2) {
                if(record.get('lifeCycleStates') && record.get('lifeCycleStates').length){
                    me.openInfoWindow(record);
                }
            }
        }, me);

        me.callParent(arguments);
    },

    //private
    statesColumnRenderer: function(value, metaData, record){
        var states = record.get('lifeCycleStates'),
            qtip;


        function addCategoryAndNames (states) {
            var result = '',
                count = 0;
            Ext.Array.each(states, function (s) {
                if(count < 5){
                    result += Ext.String.format('{0} ({1})', s.name, s.usagePointLifeCycleName) + '<br>';
                    count++;
                }
            });
            result += count < 5 ? '' : '...';
            result += '<br>' +Uni.I18n.translate('validation.clickForMoreInformation', 'IMT', 'Click for more information');
            return result;
        }

        if(record.get('lifeCycleStates') && record.get('lifeCycleStates').length){
            qtip = addCategoryAndNames(states)
        } else {
            qtip = Uni.I18n.translate('validation.ruleSetAllStates', 'IMT', 'The validation rule set is applied to data in all the usage point states')
        }

        return Ext.String.format(
            '<span style="display: inline-block; float: left; margin: 0px 10px 0px 0px">{0}</span>' +
            '<span id = "states-info-tooltip" style="display: inline-block; float: left; width: 16px; height: 16px; margin-top: -3px; cursor:pointer" class="uni-icon-info-small" data-qtip="{1}">  </span>',
            value,
            qtip
        );
    },

    //private
    openInfoWindow: function (record) {
        var widget = Ext.widget('states-info-window', {
                statesStore: Ext.create('Ext.data.Store', {
                    fields: ['name', 'stage', 'usagePointLifeCycleName'],
                    data: record.get('lifeCycleStates')
                })
            });

        widget.show();
    }
});
