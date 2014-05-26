Ext.define('Isu.view.administration.communicationtasks.View', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.communicationtasks.List',
        'Isu.view.administration.communicationtasks.Item'
    ],
    alias: 'widget.communication-tasks-view',
    content: [
        {
            title: 'Communication tasks',
            ui: 'large',
            items: [

                {
                    xtype: 'communication-tasks-list'
                },
                {
                    xtype: 'panel',
                    itemId: 'emptyPanel',
                    hidden: true,
                    height: 200,
                    items: [
                        {
                            xtype: 'panel',
                            html: "<h3>No communication tasks found</h3><br>\
          There are no communication tasks. This could be because:<br>\
          &nbsp;&nbsp; - No communication tasks have been defined yet.<br>\
          &nbsp;&nbsp; - No communication tasks comply to the filter.<br><br>\
          Possible steps:<br><br>"
                        },
                        {
                            xtype: 'button',
                            text: 'Create communication task',
                            action: 'createcommunicationtasks',
                            hrefTarget: '',
                            href: '#/issue-administration/communicationtasks/create'
                        }
                    ]
                },
                {
                    xtype: 'communication-tasks-item'
                }
            ]
        }
    ]
});