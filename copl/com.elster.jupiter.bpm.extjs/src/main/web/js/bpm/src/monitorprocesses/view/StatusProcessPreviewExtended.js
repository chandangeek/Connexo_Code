/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.StatusProcessPreviewExtended', {
    extend: 'Ext.form.Panel',
    alias: 'widget.bpm-status-process-preview-extended',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'gridpanel',
            maxHeight: 366,
            itemId: 'process-nodes-grid-extended',
            flex: 55,
            columns: {
                items: [
                    {
                        header: Uni.I18n.translate('bpm.process.node.status', 'BPM', ' Status'),
                        dataIndex: 'nodeInfo.status',
                        flex: 1,
                        renderer: function (value,metaData) {
                            metaData.tdCls = 'communication-tasks-status';
                            var template = '';

                            switch(value) {
                                case 'COMPLETED':
                                    template += '<tpl><span class="icon-checkmark"></span>';
                                    break;
                                case 'ACTIVE':
                                    template += '<tpl><span class="icon-flickr2"></span>';
                                    break;
                                case 'ABORTED':
                                    template += '<tpl><span class="icon-cross"></span>';
                                    break;
                            }
                            return template;
                        }
                    },
                    {
                        header: Uni.I18n.translate('bpm.process.node.node', 'BPM', ' Node'),
                        dataIndex: 'nodeInfo.name',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('bpm.process.node.startedOn', 'BPM', ' Started on'),
                        dataIndex: 'nodeInfo.logDateDisplay',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('bpm.process.node.type', 'BPM', ' Type'),
                        dataIndex: 'nodeInfo.type',
                        flex: 2
                    }
                ]
            }
        },
        {
        	xtype: 'container',
        	flex: 45,
            layout: {
        	type: 'vbox',
        	align: 'stretch'
    		},
    		items: [
    			{
            		xtype: 'panel',
            		flex: 1,
            		itemId: 'node-variables-preview-panel',
            		margin: '0 0 20 20',
            		frame: true,
            		autoScroll: true,
            		header : {
		                height : 32
            		},
            		items:[]
        		},
        		{
        			xtype: 'container',
        			flex: 1,
            		layout: {
        			type: 'hbox',
        			align: 'stretch'
    				},
    				items: [
	        			{
	            			xtype: 'panel',
            				flex: 1,
	            			itemId: 'child-process-preview-panel',
            				margin: '0 0 0 20',
            				frame: true,
            				autoScroll: true,
            				header : {
				                height : 32
	            			},
	    	        		items:[]
    	    			},
    	    			{
	            			xtype: 'panel',
	            			title: Uni.I18n.translate('bpm.process.node.parentProcessTitle', 'BPM', 'Parent process'),
            				flex: 1,
	            			itemId: 'parent-process-preview-panel',
            				margin: '0 0 0 20',
            				frame: true,
            				autoScroll: true,
            				header : {
				                height : 32
	            			},
	    	        		items:[]
    	    			}
    	    		]
        		}
        	]
        }
    ]
});
