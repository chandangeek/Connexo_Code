Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypeAddMeasurementTypesGrid',
    itemId: 'loadProfileTypeAddMeasurementTypesGrid',
    store: 'RegisterTypes',
    height: 395,
    scroll: false,
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    selType: 'checkboxmodel',
    selModel: {
        checkOnly: true,
        enableKeyNav: false,
        showHeaderCheckbox: false
    },
    initComponent: function () {
        this.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: 'OBIS code',
                dataIndex: 'obisCode',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 2
            },
            {
                header: Uni.I18n.translate('registerMappings.CIMreadingType', 'MDC', 'CIM Reading type'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3,
                renderer: function (value, metaData, record) {
                    var id = Ext.id();
                    Ext.defer(function () {
                        Ext.widget('button', {
                            renderTo: id,
                            icon: '../mdc/resources/images/information.png',
                            cls: 'uni-btn-transparent',
                            handler: function (item, test) {
                                this.fireEvent('showReadingTypeInfo', record);
                            },
                            itemId: 'loadProfileReadingTypeBtn'
                        });
                    }, 50);
                    return Ext.String.format('<div id="{0}">{1}</div>',  id , record.getReadingType().get('mrid'));
                }
            },
        ];
        this.callParent();
    }
});