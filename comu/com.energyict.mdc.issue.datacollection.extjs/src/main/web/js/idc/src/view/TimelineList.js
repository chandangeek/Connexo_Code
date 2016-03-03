Ext.define('Idc.view.TimelineList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-timeline',

    ui: 'medium',
    buttonAlign: 'left',
    items: [
        {
            xtype: 'no-items-found-panel',
            itemId: 'no-issue-timeline',
            title: Uni.I18n.translate('general.noTimeline','ISU','No timeline entries'),
            reasons: [
                'No activity yet on this issue'
            ],
            hidden: true
        },
        {
            xtype: 'dataview',
            itemId: 'issue-timeline-view',
            title: Uni.I18n.translate('issue.userImages','ISU','User Images'),
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
                '<p><span class="isu-icon-USER"></span><b>{user}</b> {actionText} - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',

                '<tpl if="forProcess &gt;= true">',
                    '<p><a href="javascript:void(0);" class="clickable">{processId}</a> - {contentText} {status}</p>',
                '<tpl else>',
                    '<p><tpl for="contentText">',
                    '{.}</br>',
                    '</tpl></p>',
                '</tpl>',
                '</tpl>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Uni.DateTime.formatDateTimeLong(date);
                    },
                }
            ),
            afterRender: function(){
                this.el.on('click', function(event, target){
                    this.fireEvent("onClickLink", target.innerHTML);
                }, this, {delegate: '.clickable'});
            },
            header: 'Name',
            dataIndex: 'name'
        }

    ],

});