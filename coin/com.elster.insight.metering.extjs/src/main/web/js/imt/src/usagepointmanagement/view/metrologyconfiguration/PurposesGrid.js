Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purposes-grid',
    requires: [
        'Imt.usagepointmanagement.view.metrologyconfiguration.PurposeActionMenu'
    ],
    store: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 4,
                renderer: function (value, metaData, record) {
                    var description = record.get('description');

                    return value + (description
                            ? '<span class="icon-info" style="color: #A9A9A9; margin-left: 10px; font-size: 16px; vertical-align: middle;" data-qtip="'
                        + description + '"></span>'
                            : '');
                }
            },
            {
                text: Uni.I18n.translate('general.required', 'IMT', 'Required'),
                dataIndex: 'required',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                text: Uni.I18n.translate('validation.active', 'IMT', 'Active'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                text: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value) {
                    var icon = '&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon ' + (value.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle') + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                            + value.name
                            + '"></i>';

                    return value ? icon : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Imt.privileges.UsagePoint.admin,
                itemId: 'usage-point-purpose-action-column',
                menu: {
                    xtype: 'purpose-action-menu',
                    itemId: 'usage-point-purpose-action-menu'
                },
                showCondition: function (record) {
                    return !record.get('required');
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                isFullTotalCount: true,
                noBottomPaging: true,
                displayMsg: Uni.I18n.translate('metrologyConfigurationDetails.purposesCount', 'IMT', '{2} purpose(s)')
            }
        ];
        me.callParent(arguments);
    }
});
