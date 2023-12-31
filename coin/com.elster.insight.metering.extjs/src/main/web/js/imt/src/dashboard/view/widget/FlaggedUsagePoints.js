Ext.define('Imt.dashboard.view.widget.FlaggedUsagePoints', {
    extend: 'Imt.dashboard.view.widget.FlaggedItems',
    alias: 'widget.flagged-usage-points',
    requires: [
        'Imt.dashboard.store.FlaggedUsagePoints',
        'Imt.usagepointmanagement.model.UsagePointFavorite'
    ],
    store: 'Imt.dashboard.store.FlaggedUsagePoints',
    saveModel: 'Imt.usagepointmanagement.model.UsagePointFavorite',
    emptyText: Uni.I18n.translate('overview.widget.flaggedUsagePoints.noUsagePointsFound', 'IMT', 'No flagged usage points found'),
    clickToRemoveText: Uni.I18n.translate('overview.widget.flaggedUsagePoints.unflag', 'IMT', 'Click to remove from the list of flagged usage points'),
    clickToAddText: Uni.I18n.translate('overview.widget.flaggedUsagePoints.flag', 'IMT', 'Click to flag the usage point'),
    titleTxt: Uni.I18n.translate('overview.widget.flaggedUsagePoints.header', 'IMT', 'My flagged usage points ({0})'),
    propertyName: 'name',
    tooltipTpl: new Ext.XTemplate(
        '<table>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.name', 'IMT', 'Name') + '</td>',
        '<td>{[Ext.htmlEncode(values.name)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.serviceCategory', 'IMT', 'Service category') + '</td>',
        '<tpl if="values.displayServiceCategory">',
        '<td>{[Ext.htmlEncode(values.displayServiceCategory)]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.metrologyConfiguration', 'IMT', 'Metrology configuration') + '</td>',
        '<tpl if="values.displayMetrologyConfiguration">',
        '<td>{[Ext.htmlEncode(values.displayMetrologyConfiguration)]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.type', 'IMT', 'Type') + '</td>',
        '<td>{[Ext.htmlEncode(values.displayType)]}</td>',
        '</tr>',
        '<tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.lifecycleState', 'IMT', 'State') + '</td>',
        '<td>{[Ext.htmlEncode(values.state)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.connectionState', 'IMT', 'Connection state') + '</td>',
        '<tpl if="values.displayConnectionState">',
        '<td>{[Ext.htmlEncode(values.displayConnectionState)]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.creationDate', 'IMT', 'Creation date') + '</td>',
        '<tpl if="values.creationDate">',
        '<td>{[Uni.DateTime.formatDateTimeLong(new Date(values.creationDate))]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.flaggedDate', 'IMT', 'Flagged date') + '</td>',
        '<tpl if="values.flaggedDate">',
        '<td>{[Uni.DateTime.formatDateTimeLong(new Date(values.flaggedDate))]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePoints.usagePoint.comment', 'IMT', 'Comment') + '</td>',
        '<tpl if="values.comment">',
        '<td>{[Ext.htmlEncode(values.comment)]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '</table>'
    ),

    getHref: function (item) {
        return this.router.getRoute('usagepoints/view').buildUrl({usagePointId: encodeURIComponent(item.get('name'))});
    },

    canFlag: function () {
        return Imt.privileges.UsagePoint.canFlag();
    }

});