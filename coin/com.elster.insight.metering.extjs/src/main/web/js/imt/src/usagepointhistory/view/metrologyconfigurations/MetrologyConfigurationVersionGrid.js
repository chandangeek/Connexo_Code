/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersionGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrology-configuration-version-grid',
    store: 'Imt.usagepointhistory.store.MetrologyConfigurationsHistory',
    router: null,
    requires: [
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.current', 'IMT', 'Current'),
                dataIndex: 'current',
                align: 'center',
                flex: 1,
                renderer: function(value){
                    return value ? '<i class="icon icon-checkmark-circle " style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                    + Uni.I18n.translate('general.current', 'IMT', 'Current')
                    + '"></i>' : ''
                }
            },
            {
                header: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                dataIndex: 'period',
                flex: 4
            },
            {
                header: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                dataIndex: 'metrologyConfiguration',
                flex: 4,
                renderer: function(value){
                    if(Imt.privileges.MetrologyConfig.canView()){
                        var url = me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: value.id});
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>'
                    } else {
                        return Ext.String.htmlEncode(value.name);
                    }

                }
            },
            {
                header: Uni.I18n.translate('general.ongoingProcesses', 'IMT', 'Ongoing processes'),
                dataIndex: 'ongoingProcessesNumber',
                flex: 2,
                align: 'right',
                renderer: function(value){
                    return value ? value : '-' ;
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                exportButton: false,
                isFullTotalCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagePoint.metrologyConfigurations.history.pagingtoolbartop.displayMsgs', 'IMT', '{2} metrology configuration(s)'),
              }
        ];
        me.callParent(arguments);
    }
});

