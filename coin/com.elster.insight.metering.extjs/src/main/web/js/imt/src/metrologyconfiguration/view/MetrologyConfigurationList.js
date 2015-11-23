Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrologyConfigurationList',
    requires: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu',
        'Imt.privileges.MetrologyConfig'
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
            header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
            flex: 1,
            dataIndex: 'name', 
            renderer: function (value, b, record) {
            	 return '<a href="#/administration/metrologyconfiguration/' + record.get('id') + '/view">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('general.label.createdDate', 'IMT', 'Created date'),
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
            header: Uni.I18n.translate('general.label.updatedDate', 'IMT', 'Updated date'),
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
        	privileges: Imt.privileges.MetrologyConfig.admin,
            xtype: 'uni-actioncolumn',
            menu: {
            	privileges: Imt.privileges.MetrologyConfig.admin,
                xtype: 'metrology-configuration-action-menu'
            }
        }
    ];
    me.dockedItems = [
              {
                  xtype: 'pagingtoolbartop',
                  store: me.store,
                  dock: 'top',
                  displayMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} metrology configurations'),
                  displayMoreMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} metrologyconfigurations'),
                  emptyMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.emptyMsg', 'IMT', 'There are no metrology configurations to display'),
                  items: [
                          {
                        	  text: Uni.I18n.translate('metrologyconfiguration.button.add', 'IMT', 'Add metrology configuration'),
                        	  itemId: 'createMetrologyConfiguration',
                        	  xtype: 'button',
                        	  privileges: Imt.privileges.MetrologyConfig.admin,
                        	  action: 'createMetrologyConfiguration',
                          }
                  ]
              },
              {
                  xtype: 'pagingtoolbarbottom',
                  store: me.store,
                  dock: 'bottom',
                  itemsPerPageMsg: Uni.I18n.translate('metrologyconfiguration.label.itemsperpage', 'IMT', 'Metrology configuration per page'),
              },
              
          ];
        me.callParent(arguments);
    }
});