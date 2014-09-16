Ext.define('Isu.view.workspace.issues.Item', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-item',
    itemId: 'issues-item',
    requires: [
        'Isu.view.workspace.issues.ActionMenu',
        'Isu.view.workspace.issues.FormWithFilters'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'issue-action-menu'
            }
        }
    ],
    items: {
        itemId: 'issue-form-with-filters',
        xtype: 'issue-form-with-filters',
        bbar: {
            layout: {
                type: 'vbox',
                align: 'right'
            },
            items: {
                text: Uni.I18n.translate('general.title.viewDetails', 'ISU', 'View details'),
                itemId: 'viewDetails',
                ui: 'link',
                action: 'view',
                listeners: {
                    click: function () {
                        window.location.href = "#/workspace/datacollection/issues/" + this.up('form').getRecord().get('id')
                    }
                }
            }
        }
    }
});