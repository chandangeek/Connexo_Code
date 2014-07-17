Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileConfigurationDetailChannelGrid',
    itemId: 'loadProfileConfigurationDetailChannelGrid',
    store: 'LoadProfileConfigurationDetailChannels',
    height: 395,
    scroll: false,
    editActionName: null,
    deleteActionName: null,

    requires: [
        'Uni.grid.column.Obis'
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
                header: Uni.I18n.translate('registerMappings.CIMreadingType', 'MDC', 'CIM Reading type'),
                dataIndex: 'name',
                flex: 3,
                renderer: function (value, metaData, record) {
                    var id = Ext.id();
                    Ext.defer(function () {
                        Ext.widget('button', {
                            renderTo: id,
                            icon: '../ext/packages/uni-theme-skyline/resources/images/shared/icon-info-small.png',
                            cls: 'uni-btn-transparent',
                            handler: function (item, test) {
                                this.fireEvent('showReadingTypeInfo', record.getMeasurementType());
                            },
                            itemId: 'channelsReadingTypeBtn'
                        });
                    }, 50);
                    return Ext.String.format('<div id="{0}">{1}</div>',  id, record.getMeasurementType().getReadingType().get('mrid'));
                }
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