/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationPurposesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrology-config-purposes-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],

    initComponent: function () {
        var me = this;

        me.features = [{
            ftype: 'grouping',
            groupHeaderTpl: [
                '{name} {name:this.isRequired}',
                {
                    isRequired: function (name) {
                        var store = me.getStore(),
                            isRequired = store.getAt(store.find('metrologyContract', name)).get('metrologyContractIsMandatory');

                        return isRequired ? ' <span class="uni-form-item-label-required" style="display: inline-block; width: 16px; height: 16px;"></span>' : '';
                    }
                }
            ]
        }];

        me.columns = [
            {
                header: Uni.I18n.translate('general.outputName', 'IMT', 'Output name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.readingType', 'IMT', 'Reading type'),
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getReadingType().get('fullAliasName');
                }
            },
            {
                header: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getFormula().get('description');
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('metrologyConfigPurposes.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} purposes'),
                displayMoreMsg: Uni.I18n.translate('metrologyConfigPurposes.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} purposes'),
                emptyMsg: Uni.I18n.translate('metrologyConfigPurposes.pagingtoolbartop.emptyMsg', 'IMT', 'There are no purposes to display'),
                noBottomPaging: true,
                usesExactCount: true
                // out of scope CXO-633
                //items: [
                //    {
                //        xtype: 'button',
                //        itemId: 'metrology-config-add-purpose-btn',
                //        text: Uni.I18n.translate('metrologyConfigPurposes.add', 'IMT', 'Add purpose'),
                //        privileges: Imt.privileges.MetrologyConfig.admin,
                //        action: 'addPurpose'
                //    }
                //]
            }
        ];

        me.callParent(arguments);
    }
});