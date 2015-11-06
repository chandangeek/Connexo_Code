Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrologyConfigurationList',
    requires: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu'
    ],
    store: 'Imt.metrologyconfiguration.store.MetrologyConfiguration',
    overflowY: 'auto',
    itemId: 'metrologyConfigurationList',
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true
    },
    initComponent: function () {
        var me = this;
        me.columns = [
        {
            header: Uni.I18n.translate('metrologyconfigurations.name', 'IMT', 'Name'),
            flex: 1,
            dataIndex: 'name', 
        },
        {
            header: Uni.I18n.translate('metrologyconfigurations.createdDate', 'IMT', 'Created date'),
            flex: 1,
            dataIndex: 'created',
            renderer: function(value){
                if(!Ext.isEmpty(value)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                }
                return '-';
            }
        },
        {
            header: Uni.I18n.translate('metrologyconfigurations.updatedDate', 'IMT', 'Updated date'),
            flex: 1,
            dataIndex: 'updated',
            renderer: function(value){
                if(!Ext.isEmpty(value)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                }
                return '-';
            }
        },
        {
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'metrology-configuration-action-menu'
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
                  displayMsg: '{2} metrology configuration(s)',
                  items: [
                          {
                        	  text: Uni.I18n.translate('metrologyconfigurations.addMetrologyConfiguration', 'IMT', 'Add metrology configuration'),
                        	  itemId: 'createMetrologyConfiguration',
                        	  xtype: 'button',
                        	  action: 'createMetrologyConfiguration',
                          }
                  ]
              }
          ];
        me.callParent(arguments);
    }
});