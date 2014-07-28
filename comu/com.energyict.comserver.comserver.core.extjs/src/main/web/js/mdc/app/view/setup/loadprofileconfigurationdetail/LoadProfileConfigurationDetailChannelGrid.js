Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileConfigurationDetailChannelGrid',
    itemId: 'loadProfileConfigurationDetailChannelGrid',
    store: 'LoadProfileConfigurationDetailChannels',
    maxHeight: 395,
    scroll: false,
    editActionName: null,
    deleteActionName: null,

    requires: [
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },

    initComponent: function () {
        this.columns = [
            {
                header: 'Measurement Type',
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'reading-type-column',
                header: Uni.I18n.translate('registerMappings.CIMreadingType', 'MDC', 'CIM Reading type'),
                dataIndex: 'readingType'
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode'
            },
            {
                xtype: 'uni-actioncolumn',
                items: [

                    {
                        text: 'Edit',
                        action: this.editActionName
                    },
                    {
                        text: 'Remove',
                        action: this.deleteActionName
                    }
                ]
            }
        ];
        this.callParent();
    }
});