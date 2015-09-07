Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileConfigurationGrid',
    itemId: 'loadProfileConfigurationGrid',
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Obis',
        'Mdc.store.Intervals'
    ],
    columns: {
        items: [
            {
                header: 'Load profile type',
                xtype: 'templatecolumn',
                tpl: new Ext.XTemplate('{[this.getLink(values)]}', {
                    getLink: function (values) {
                        var config = Ext.ComponentQuery.query('loadProfileConfigurationSetup')[0].config;

                        return Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}/loadprofiles/{2}/channels">{3}</a>', config.deviceTypeId, config.deviceConfigurationId, values.id, Ext.String.htmlEncode(values.name));
                    }
                }),
                renderer: false,
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode'
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                    return intervalRecord ? Ext.String.htmlEncode(intervalRecord.get('name')) : '';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                items: 'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} load profile configurations'),
            displayMoreMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} load profile configurations'),
            emptyMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbartop.emptyMsg', 'MDC', 'There are no load profile configurations to display'),
            items: [
                {
                    text: Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration'),
                    privileges: Mdc.privileges.DeviceType.admin,
                    action: 'addloadprofileconfiguration'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Load profile configurations per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});

