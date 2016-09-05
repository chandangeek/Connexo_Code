Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purposes-grid',
    store: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 4
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
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyConfigurationDetails.purposesCount', 'IMT', '{0} purpose(s)', me.store.getCount())
            }
        ];
        me.callParent(arguments);
    }
});
