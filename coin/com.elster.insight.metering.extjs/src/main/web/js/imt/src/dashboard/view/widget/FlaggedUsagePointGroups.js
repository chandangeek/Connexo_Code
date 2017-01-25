Ext.define('Imt.dashboard.view.widget.FlaggedUsagePointGroups', {
    extend: 'Imt.dashboard.view.widget.FlaggedItems',
    alias: 'widget.flagged-usage-point-groups',
    requires: [
        'Imt.dashboard.store.FlaggedUsagePointGroups',
        'Imt.usagepointgroups.model.UsagePointGroupFavorite'
    ],
    store: 'Imt.dashboard.store.FlaggedUsagePointGroups',
    saveModel: 'Imt.usagepointgroups.model.UsagePointGroupFavorite',
    emptyText: Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.noUsagePointGroupsFound', 'IMT', 'No favorite usage point groups found'),
    clickToRemoveText: Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.unflag', 'IMT', 'Click to remove from the list of favorite usage point groups'),
    clickToAddText: Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.flag', 'IMT', 'Click to flag the usage point group'),
    titleTxt: Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.header', 'IMT', 'My favorite usage point groups ({0})'),
    propertyName: 'id',
    showSelectButton: true,
    tooltipTpl: new Ext.XTemplate(
        '<table>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.usagePointGroup.name', 'IMT', 'Name') + '</td>',
        '<td>{[Ext.htmlEncode(values.name)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.usagePointGroup.type', 'IMT', 'Type') + '</td>',
        '<tpl if="values.dynamic">',
        '<td>{[Uni.I18n.translate("general.dynamic", "IMT", "Dynamic")]}</td>',
        '<tpl else>',
        '<td>{[Uni.I18n.translate("general.static", "IMT", "Static")]}</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.usagePointGroup.flaggedDate', 'IMT', 'Flagged date') + '</td>',
        '<tpl if="values.flaggedDate">',
        '<td>{[Uni.DateTime.formatDateTimeLong(new Date(values.flaggedDate))]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '<tr>',
        '<td style="vertical-align: top; text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedUsagePointGroups.usagePointGroup.comment', 'IMT', 'Comment') + '</td>',
        '<tpl if="values.comment">',
        '<td>{[Ext.htmlEncode(values.comment)]}</td>',
        '<tpl else>',
        '<td>-</td>',
        '</tpl>',
        '</tr>',
        '</table>'
    ),

    getHref: function (item) {
        return this.router.getRoute('usagepoints/usagepointgroups/view').buildUrl({usagePointGroupId: encodeURIComponent(item.get('id'))});
    },

    canFlag: function () {
        return Imt.privileges.UsagePointGroup.canFlag();
    }

});