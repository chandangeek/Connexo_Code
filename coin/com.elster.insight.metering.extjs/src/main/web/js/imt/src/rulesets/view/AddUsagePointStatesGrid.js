/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.view.AddUsagePointStatesGrid', {
    extend: 'Uni.view.grid.SelectionGridForPreviewContainer',
    alias: 'widget.add-usage-point-states-grid',
    isCheckedFieldName: 'isChecked',
    checkAllButtonPresent: true,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('ruleSet.addStates.grid.multiselect.selected', count, 'IMT',
            'No states selected', '{0} state selected', '{0} states selected'
        );
    },
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.state', 'IMT', 'State'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.stage', 'IMT', 'Stage'),
                dataIndex: 'stage',
                renderer: function (value) {
                    if(!Ext.isEmpty(value.name)) {
                        return value.name
                    }
                    return '-';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.usagePointLifeCycle', 'IMT', 'Usage point life cycle'),
                dataIndex: 'usagePointLifeCycleName',
                flex: 1
            }
        ];
        me.callParent(arguments);
    }
});