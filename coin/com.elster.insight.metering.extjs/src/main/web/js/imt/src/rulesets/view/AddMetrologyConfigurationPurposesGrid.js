/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.view.AddMetrologyConfigurationPurposesGrid', {
    extend: 'Uni.view.grid.SelectionGridForPreviewContainer',
    alias: 'widget.add-metrology-configuration-purposes-grid',
    isCheckedFieldName: 'isChecked',
    checkAllButtonPresent: true,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('ruleSet.addMetrologyConfigurationPurposes.grid.multiselect.selected', count, 'IMT',
            'No purposes of metrology configurations selected', '{0} purpose of metrology configurations selected', '{0} purposes of metrology configurations selected'
        );
    },
    router: null,

    initComponent: function () {
        var me = this,
            route = me.router.getRoute('administration/metrologyconfiguration/view');

        me.columns = [
            {
                header: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                dataIndex: 'metrologyConfigurationInfo',
                flex: 1,
                renderer: function (value) {
                    if (Imt.privileges.MetrologyConfig.canView() || Imt.privileges.MetrologyConfig.canAdministrate()) {
                        return Ext.String.format('<a href="{0}">{1}</a>', route.buildUrl({mcid: value.id}), value.name);
                    } else {
                        return value.name;
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.metrologyConfigurationStatus', 'IMT', 'Metrology configuration status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.active', 'IMT', 'Active')
                        : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                }
            },
            {
                header: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                dataIndex: 'name',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});