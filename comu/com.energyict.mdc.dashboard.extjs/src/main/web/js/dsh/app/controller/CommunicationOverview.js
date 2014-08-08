Ext.define('Dsh.controller.CommunicationOverview', {
    extend: 'Ext.app.Controller',
    stores: [
        'CommunicationServerInfos',
        'OverviewPerCurrentStateInfos',
        'OverviewPerLastResultInfos'
    ],
    views: [
        'Dsh.view.widget.HSeparator',
        'Dsh.view.widget.OverviewHeader',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown',
        'Dsh.view.CommunicationOverview'
    ],
    refs: [],
    init: function () {
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('communication-overview');
        widget.add(Ext.widget('overview-header', { headerTitle: 'Communication overview' })); //TODO: localize
        widget.add(Ext.widget('summary', { title: 'Communication summary' }));
        widget.add(Ext.widget('communication-servers'));
        widget.add(Ext.widget('quicklinks', {
            data: [ //TODO: check & change
                { link: 'View all communication tasks', href: '#/workspace/datacommunication/communications' },
                { link: 'Connection overview', href: '#/workspace/datacommunication/connection' },
                { link: 'Some link 1', href: '#' },
                { link: 'Some link 2', href: '#' },
                { link: 'Some link 3', href: '#' }
            ]
        }));
        widget.add(Ext.widget('h-sep'));
        widget.add(Ext.widget('read-outs-over-time'));
        widget.add(Ext.widget('h-sep'));
        widget.add(Ext.widget('overview'));
        widget.add(Ext.widget('h-sep'));
        widget.add(Ext.widget('breakdown'));
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});