Ext.define('Bpm.controller.ProcessInstances', {
    extend: 'Ext.app.Controller',
    requires: [
    ],
    stores: [
        'Bpm.store.ProcessInstances',
        'Bpm.store.Variables',
        'Bpm.store.Nodes'
    ],
    views: [
        'Bpm.view.instance.Browse',
        'Bpm.view.instance.Overview'
    ],

    refs: [
        { ref: 'instanceBrowse', selector: 'instanceBrowse' },
        { ref: 'instanceList', selector: 'instanceBrowse instanceList' },
        { ref: 'instanceDetails', selector: 'instanceBrowse instanceDetails' }
    ],

    init: function () {
        this.control({
            'instanceBrowse instanceList': {
                selectionchange: this.selectInstance
            }
        });
    },

    showProcessInstances : function () {
        var widget = Ext.widget('instanceBrowse');
        this.getApplication().getController("Bpm.controller.Main").showContent(widget);
    },

    showProcessInstanceOverview : function (deploymentId, id) {
        var me=this,
            processInstance = Ext.ModelManager.getModel('Bpm.model.ProcessInstance');

        var widget = Ext.widget('instanceOverview');
        me.getApplication().getController("Bpm.controller.Main").showContent(widget);

        widget.setLoading(true);
        processInstance.getProxy().setExtraParam('deploymentId', deploymentId);
        processInstance.load(id, {
            success: function (record) {
                widget.setLoading(false);
                var title = Uni.I18n.translate('bpm.instance.overview.title',  'BPM', 'Process {0} of \'{1}\'');
                widget.down('panel').setTitle(Ext.String.format(title, record.get('id'), Ext.String.htmlEncode(record.get('name'))));
                widget.down('form').loadRecord(record);

                var variables = me.getStore('Bpm.store.Variables'),
                    nodes = me.getStore('Bpm.store.Nodes');

                me.getVariables(widget, record, variables);
                me.getNodes(widget, record, nodes, me);

                me.getApplication().fireEvent('viewProcessInstance', record);
            }
        });
    },

    getVariables : function(widget, record, variables) {
        variables.getProxy().setExtraParam('deploymentId', record.get('deploymentId'));
        variables.getProxy().setExtraParam('id', record.get('id'));
        widget.down('variableList').setLoading(true);
        variables.load(function () {
            widget.down('variableList').bindStore(variables);
            widget.down('variableList').setLoading(false);
        });
    },

    getNodes : function (widget, record, nodes, me) {
        nodes.getProxy().setExtraParam('deploymentId', record.get('deploymentId'));
        nodes.getProxy().setExtraParam('id', record.get('id'));
        widget.down('nodeList').setLoading(true);
        nodes.load(function () {
            widget.down('nodeList').bindStore(nodes);
            widget.down('nodeList').setLoading(false);
            widget.down('#currentActivities').setValue(me.getCurrentActivities(nodes))
        });
    },

    getCurrentActivities : function(nodes) {
        var lines='';
        nodes.each(function(record) {
            if (record.get('state') == 0) {
                var nodeName = record.get('nodeName');
                lines += record.get('date') + ' - ' + nodeName;
                if (nodeName != '(Join)' && nodeName != '(Split)') {
                    lines += ' (' + record.get('nodeType') +')';
                }
                lines +=  '<br />';
            }
        });
        if (lines.length == 0) {
            lines ='-';
        }
        return lines;
    },

    selectInstance: function (grid, record) {
        if(record.length > 0){
            var panel = grid.view.up('preview-container').down('instanceDetails'),
                form = panel.down('form');
            var title = Uni.I18n.translate('bpm.instance.detail.title',  'BPM', 'Process {0} of \'{1}\'');
            panel.setTitle(Ext.String.format(title, Ext.String.htmlEncode(record[0].get('id')), Ext.String.htmlEncode(record[0].get('name'))));
            form.loadRecord(record[0]);
        }
    }

});