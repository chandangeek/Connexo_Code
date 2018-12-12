/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.TimelineList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.util.FormEmptyMessage'
    ],
    alias: 'widget.alarm-timeline',

    ui: 'medium',
    buttonAlign: 'left',

    layout: {
        type: 'vbox',
        align: 'left'
    },

    items: [
        {
            xtype: 'uni-form-empty-message',
            itemId: 'no-issue-timeline',
            text: Uni.I18n.translate('general.noActivity', 'DAL', 'No activity yet on this alarm'),
            hidden: true
        },
        {
            xtype: 'dataview',
            itemId: 'issue-timeline-view',
            title: Uni.I18n.translate('issue.userImages', 'DAL', 'User Images'),
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
                '<p><span class="isu-icon-USER"></span><b>{user}</b> {actionText} - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',

                '<tpl if="forProcess &gt;= true">',
                '<p><a href="javascript:void(0);" class="clickable">{processId}</a> - {contentText} {status}</p>',
                '<tpl else>',
                '<p><tpl for="contentText">',
                '{.:htmlEncode}</br>',
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
            afterRender: function () {
                this.el.on('click', function (event, target) {
                    this.fireEvent("onClickLink", target.innerHTML);
                }, this, {delegate: '.clickable'});
            },
            header: 'Name',
            dataIndex: 'name'
        }

    ],

});