Ext.define('Isu.view.workspace.issues.FilteringToolbar', {
    extend: 'Ext.container.Container',
    requires: [
        'Isu.view.workspace.issues.component.TagButton'
    ],
    alias: 'widget.filtering-toolbar',
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    items: [
        {
            xtype: 'component',
            html: 'Filters',
            cls: 'isu-toolbar-label',
            width: 55
        },
        {
            xtype: 'component',
            html: 'None',
            name: 'empty-text'
        },
        {
            xtype: 'container',
            name: 'filter',
            header: false,
            border: false,
            margin: '10 0 10 0',
            layout: {
                type: 'hbox',
                align: 'stretch',
                defaultMargins: '0 5'
            },
            flex: 1
        },
        {
            xtype: 'button',
            action: 'clearfilter',
            text: 'Clear all',
            disabled: true
        }
    ],

    /**
     * todo: I18n
     * @param filter Uni.component.filter.model.Filter
     */
    addFilterButtons: function (filterModel) {
        var filterElm = this.down('[name="filter"]'),
            emptyText = this.down('[name="empty-text"]'),
            clearFilterBtn = this.down('button[action="clearfilter"]'),
            buttons = [],
            button;

        if (filterModel.get('assignee')) {
            button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                text: 'Assignee: ' + filterModel.get('assignee').get('name'),
                target: 'assignee'
            });

            buttons.push(button);
        }

        if (filterModel.get('reason')) {
            button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                text: 'Reason: ' + filterModel.get('reason').get('name'),
                target: 'reason'
            });

            buttons.push(button);
        }

        if (filterModel.get('meter')) {
            button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                text: 'Meter: ' + filterModel.get('meter').get('name'),
                target: 'meter'
            });

            buttons.push(button);
        }

        if (filterModel.status().count()) {
            filterModel.status().each(function (status) {
                button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                    text: 'Status: ' + status.get('name'),
                    target: 'status',
                    targetId: status.getId()
                });

                buttons.push(button);
            });
        }

        filterElm.removeAll();

        if (buttons.length) {
            emptyText.hide();
            clearFilterBtn.setDisabled(false);

            Ext.Array.each(buttons, function (button) {
                filterElm.add(button);
            });
        } else {
            emptyText.show();
            clearFilterBtn.setDisabled(true);
        }
    }
});