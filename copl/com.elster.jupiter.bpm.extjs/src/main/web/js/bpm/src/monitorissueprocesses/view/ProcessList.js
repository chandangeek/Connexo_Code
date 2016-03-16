Ext.define('Bpm.monitorissueprocesses.view.ProcessList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-process-list',

    ui: 'medium',
    buttonAlign: 'left',
    items: [
        {
            xtype: 'no-items-found-panel',
            itemId: 'no-issue-processes',
            title: Uni.I18n.translate('processes.issue.noProcesses','BPM','No processes found'),
            reasons: [
                Uni.I18n.translate('processes.issue.noProcessesStarted','BPM','No process started yet on this issue')
            ],
            hidden: true
        },
        {
            xtype: 'dataview',
            itemId: 'issue-process-view',
            title: Uni.I18n.translate('issue.userImages','BPM','User Images'),
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '{[xindex > 1 ? "<hr>" : ""]}',
                    '<p> {statusIcon} </span> <b>{startDateDisplay}</b> - <a href="javascript:void(0);" class="clickable">{processId}</a> - {name} ' +
                    '(' + Uni.I18n.translate('bpm.process.version', 'BPM', 'Version')  +
                    ' {version}' + '), ' +
                    Uni.I18n.translate('bpm.process.startedBy', 'BPM', 'Started by') +
                    '  <b>{startedBy}</b></p>',
                    '<tpl if="this.hasOpenTasks(openTasks)">',
                        '<table width="400"><tr>',
                            '<td width="40%" align="center" valign="top">',
                            '<p><b>' + Uni.I18n.translate('bpm.process.openTasks', 'BPM', 'Open tasks') + '</b></p>',
                            '</td>',
                            '<td>',
                            '<tpl for="openTasks">',
                                '<p><a name="{id}" href="javascript:void(0);" style="{taskLinkStyle}" class="clickable">{name}</a> '+'('+'{statusDisplay}, {actualOwner})</p>',
                            '</tpl>',

                            '</td>',
                        '</tr></table>',
                    '</tpl>',
                '</tpl>',
                {
                    hasOpenTasks: function (openTasks) {
                        return openTasks.length > 0;
                    },
                    getTaskLinkStyle: function()
                    {
                        return (!Bpm.privileges.BpmManagement.canExecute())?"pointer-events: none; cursor: default;":"";
                    }

                }
            ),
            afterRender: function(){
                this.el.on('click', function(event, target){
                    if(target.name) {
                        if (!Bpm.privileges.BpmManagement.canExecute()) return;
                        this.fireEvent("onClickTaskLink", target.name);
                    }
                    else
                        this.fireEvent("onClickLink", target.innerHTML);
                }, this, {delegate: '.clickable'});
            },
            header: 'Name',
            dataIndex: 'name'
        }

    ]
});