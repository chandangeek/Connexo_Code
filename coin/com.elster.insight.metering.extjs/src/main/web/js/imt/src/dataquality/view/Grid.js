/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.imt-quality-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'Imt.dataquality.store.DataQuality',
    router: null,
    hasHtmlInColumnHeaders: true,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                exportText: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                dataIndex: 'usagePointName',
                flex: 4,
                renderer: function (value) {
                    var url = me.router.getRoute('usagepoints/view').buildUrl({
                        usagePointId: encodeURIComponent(value)
                    });
                    return Imt.privileges.UsagePoint.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>' : value;
                }
            },
            {
                header: Uni.I18n.translate('general.serviceCategory', 'IMT', 'Service category'),
                exportText: Uni.I18n.translate('general.serviceCategory', 'IMT', 'Service category'),
                dataIndex: 'serviceCategory',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                exportText: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                dataIndex: 'metrologyConfiguration',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({
                            mcid: value.id
                        }),
                        metrologyConfiguration = Imt.privileges.MetrologyConfig.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name),
                        icon = '<span class="icon-history" style="margin-left:10px;" data-qtip="' + Uni.I18n.translate('metrologyConfiguration.inThePast', 'IMT', 'Metrology configuration in the past') + '"></span>';

                    return !!record.get('isEffectiveConfiguration') ? metrologyConfiguration : metrologyConfiguration + icon;
                },
                flex: 5
            },
            {
                header: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                exportText: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                dataIndex: 'metrologyContract',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('usagepoints/view/purpose').buildUrl({
                        usagePointId: encodeURIComponent(record.get('usagePointName')),
                        purposeId: value.id
                    });
                    return Imt.privileges.UsagePoint.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name);
                },
                flex: 3
            },
            {
                header: '<span class="white-circle-grid-header icon-flag5" style="color:red;" data-qtip="' + Uni.I18n.translate('general.suspects', 'IMT', 'Suspects') + '"></span>',
                exportText: Uni.I18n.translate('general.suspects', 'IMT', 'Suspects'),
                dataIndex: 'amountOfSuspects',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-checkmark" style="color:#686868" data-qtip="' + Uni.I18n.translate('general.confirmed', 'IMT', 'Confirmed') + '"></span>',
                exportText: Uni.I18n.translate('general.confirmed', 'IMT', 'Confirmed'),
                dataIndex: 'amountOfConfirmed',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-flag5" style="color:#33CC33;" data-qtip="' + Uni.I18n.translate('general.estimates', 'IMT', 'Estimates') + '"></span>',
                exportText: Uni.I18n.translate('general.estimates', 'IMT', 'Estimates'),
                dataIndex: 'amountOfEstimates',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-flag5" style="color:#dedc49;" data-qtip="' + Uni.I18n.translate('general.informatives', 'IMT', 'Informatives') + '"></span>',
                exportText: Uni.I18n.translate('general.informatives', 'IMT', 'Informatives'),
                dataIndex: 'amountOfInformatives',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-pencil4" style="color:#686868" data-qtip="' + Uni.I18n.translate('general.edited', 'IMT', 'Edited') + '"></span>',
                exportText: Uni.I18n.translate('general.edited', 'IMT', 'Edited'),
                dataIndex: 'amountOfTotalEdited',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header" style="font-weight:bold; cursor: default; color:#686868;font-size: 10px;"  data-qtip="' + Uni.I18n.translate('general.projected', 'IMT', 'Projected') + '">&nbsp;&nbsp;P&nbsp;</span>',
                exportText: Uni.I18n.translate('general.projected', 'IMT', 'Projected'),
                dataIndex: 'amountOfProjected',
                align: 'right',
                flex: 1
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                needCustomExporter: true,
                displayMsg: Uni.I18n.translate('dataQuality.paging.displayMsg', 'IMT', '{0} - {1} of {2} usage points'),
                displayMoreMsg: Uni.I18n.translate('dataQuality.paging.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} usage points'),
                emptyMsg: Uni.I18n.translate('dataQuality.paging.emptyMsg', 'IMT', 'There are no usage points to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                deferLoading: true,
                dock: 'bottom',
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('dataQuality.paging.usagePointsPerPage', 'IMT', 'Usage points per page')
            }
        ];

        this.callParent(arguments);
    }
});
