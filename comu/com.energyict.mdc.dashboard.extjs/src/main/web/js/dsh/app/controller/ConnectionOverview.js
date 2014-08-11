Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.BreakdownCounter',
        'Dsh.model.ConnectionCounter',
        'Dsh.model.ConnectionBreakdown',
        'Dsh.model.ConnectionOverview',
        'Dsh.model.ConnectionSummary',
        'Dsh.model.CommunicationServerInfo',
        'Dsh.model.OverviewPerCurrentStateInfo',
        'Dsh.model.OverviewPerLastResultInfo',
        'Dsh.model.TimeInfo',
        'Dsh.model.CounterInfo'
    ],
    stores: [
        'CommunicationServerInfos',
        'OverviewPerCurrentStateInfos',
        'OverviewPerLastResultInfos'
    ],
    views: [ 'Dsh.view.ConnectionOverview' ],
    refs: [
        { ref: 'breakdown', selector: '#breakdown' }
    ],

    init: function () {
        this.control({
            '#brakdownchartcombinecombobox': {
                change: this.combineComboChanged
            }
        });
        this.callParent(arguments);
    },

    showOverview: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview'));
        this.loadBreakdownData();
    },

    combineComboChanged: function (combo, newValue) {
        var me = this;
        me.setNewChartData(combo.record, newValue);
    },

    loadBreakdownData: function () {
        var me = this;
        model = me.getModel('Dsh.model.ConnectionSummary');
        model.load(null, {
                success: function (record) {
                    var breakdowns = record.breakdowns();
                    me.getBreakdown().bindStore(breakdowns);
                }
            }
        );
    }
});