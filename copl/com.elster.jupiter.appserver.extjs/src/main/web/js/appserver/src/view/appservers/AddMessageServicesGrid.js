Ext.define('Apr.view.appservers.AddMessageServicesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-message-services-grid',
    requires: [
        'Apr.store.ActiveService'
    ],

    plugins: [
        {
            ptype: 'bufferedrenderer'
        },
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfMessageServices.selected', count, 'APR',
            'No message services selected', '{0} message service selected', '{0} message services selected'
        );
    },

    bottomToolbarHidden: true,
    checkAllButtonPresent: true,


    columns: [
        {
            header: Uni.I18n.translate('general.name', 'APR', 'Name'),
            dataIndex: 'messageService',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'APR', 'Status'),
            dataIndex: 'active',
            flex: 1,
            editor: {
                xtype: 'combobox',
                allowBlank: false,
                displayField:'displayName',
                valueField:'active',
                queryMode:'local',
                store: 'Apr.store.ActiveService'
            },
            renderer: function(value){
                return value?Uni.I18n.translate('general.active', 'APR', 'Active'):Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
            }
        },
        {
            itemId: 'threads-column',
            header: Uni.I18n.translate('general.threads', 'APR', 'Threads'),
            dataIndex: 'numberOfThreads',
            align: 'right',
            flex: 0.6,
            emptyCellText: 1,
            editor: {
                xtype: 'numberfield',
                minValue: 1
            }
        }
    ]
});


