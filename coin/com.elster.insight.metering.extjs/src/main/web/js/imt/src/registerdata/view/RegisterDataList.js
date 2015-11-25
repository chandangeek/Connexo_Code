Ext.define('Imt.registerdata.view.RegisterDataList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerDataList',
    requires: [
        'Imt.registerdata.store.RegisterData',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.registerdata.view.ActionMenu'
    ],
    store: 'Imt.registerdata.store.RegisterData',
    overflowY: 'auto',
    itemId: 'registerDataList',
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true

    },
    initComponent: function () {
        var me = this;
        me.columns = [
        {
            header: Uni.I18n.translate('general.label.reading.timestamp', 'IMT', 'Reading timestamp'),
            flex: 1,
            dataIndex: 'readingTime', 
            renderer: function(value){
                if(!Ext.isEmpty(value)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                }
                return '-';
            }
        },
        {
            header: Uni.I18n.translate('general.label.reading.value', 'IMT', 'Reading value'),
            flex: 1,
            dataIndex: 'value'
        },
        {
            header: Uni.I18n.translate('general.label.delta.value', 'IMT', 'Delta value'),
            flex: 1,
            dataIndex: 'deltaValue'
        }
    ];
        me.dockedItems = [
              {
                  xtype: 'pagingtoolbartop',
                  store: me.store,
                  dock: 'top',
                  isFullTotalCount: true,
                  noBottomPaging: true,
                  displayMsg: '{2} reading(s)'
              }
          ];
        me.callParent(arguments);
    }
});