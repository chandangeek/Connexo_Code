Ext.define('Imt.registerdata.view.RegisterDataList', {
    extend: 'Imt.registerdata.view.RegisterDataMainGrid',
    alias: 'widget.registerDataList',
    requires: [
        'Imt.registerdata.store.RegisterData',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.registerdata.view.ActionMenu',
        'Uni.grid.column.ValidationFlag'
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
	            xtype: 'validation-flag-column',
	            dataIndex: 'value',
	            align: 'right',
	            minWidth: 100,
	            flex: 1,
	            renderer: function (data, metaData, record) {
	                if (record.data.validationStatus) {
	                    var result = record.data.validationResult,
	                        status = result.split('.')[1],
	                        cls = 'icon-validation-cell';
	
	                    if (status === 'suspect') {
	                        cls +=  ' icon-validation-red'
	                    }
	                    if (status === 'notValidated') {
	                        cls +=  ' icon-validation-black'
	                    }
	                    metaData.tdCls = cls;
	                }
	                if (!Ext.isEmpty(data)) {
	                    return record.get('isConfirmed') ? Uni.Number.formatNumber(data, -1) + '<span style="margin: 0 0 0 10px; position: absolute" class="icon-checkmark3"</span>' : Uni.Number.formatNumber(data, -1);
	                }
	            }
	        },
	        {
	            xtype: 'edited-column',
	            dataIndex: 'modificationState',
	            header: '',
	            width: 30,
	            emptyText: ' '
	        },
	        {
	        	header: Uni.I18n.translate('general.label.delta.value', 'IMT', 'Delta value'),
	//            xtype: 'validation-flag-column',
	            dataIndex: 'deltaValue',
	            align: 'right',
	            width: 200,
	            flex: 1,
//	            renderer: function (data, metaData, record) {
//	                if (record.data.validationStatus2) {
//	                    var result = record.data.validationResult,
//	                        status = result.split('.')[1],
//	                        cls = 'icon-validation-cell';
//	                    if (status === 'suspect') {
//	                        cls +=  ' icon-validation-red'
//	                    }
//	                    if (status === 'notValidated') {
//	                        cls +=  ' icon-validation-black'
//	                    }
//	                    metaData.tdCls = cls;
//	                }
//	                if (!Ext.isEmpty(data)) {
//	                    return record.get('isConfirmed') ? Uni.Number.formatNumber(data, -1) + '<span style="margin: 0 0 0 10px; position: absolute" class="icon-checkmark3"</span>' : Uni.Number.formatNumber(data, -1);
//	                }
//	            }
	        },
	        {
	            xtype: 'uni-actioncolumn',
	// //           privileges: Mdc.privileges.Device.administrateDeviceData,
	// //           dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
	            menu: {
	                xtype: 'registerDataActionMenu'
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
                  displayMsg: '{2} reading(s)'
              }
          ];
        me.callParent(arguments);
    }
});