Ext.define("ExtThemeNeptune.Component",{override:"Ext.Component",initComponent:function(){this.callParent();if(this.dock&&this.border===undefined){this.border=false}},initStyles:function(){var b=this,a=b.border;if(b.dock){b.border=null}b.callParent(arguments);b.border=a}});Ext.define("ExtThemeNeptune.panel.Panel",{override:"Ext.panel.Panel",border:false,bodyBorder:false,initBorderProps:Ext.emptyFn,initBodyBorder:function(){if(this.bodyBorder!==true){this.callParent()}}});Ext.define("Skyline.panel.Panel",{override:"Ext.panel.Panel",beforeRender:function(){var a=this;this.callParent(arguments);if(a.subtitle){this.setSubTitle(a.subtitle)}},setSubTitle:function(a){var b=this,c=b.header;b.subtitle=a;if(c){if(c.isHeader){c.setSubTitle(a)}else{c.subtitle=a}}else{if(b.rendered){b.updateHeader()}}}});Ext.define("Skyline.panel.Header",{override:"Ext.panel.Header",headingTpl:['<span id="{id}-textEl" class="{headerCls}-text {cls}-text {cls}-text-{ui}" unselectable="on"','<tpl if="headerRole">',' role="{headerRole}"',"</tpl>",">{title}</span>",'<span id="{id}-subTextEl" class="{headerCls}-sub-text {cls}-sub-text {cls}-sub-text-{ui}" unselectable="on"',">{subtitle}</span>"],initComponent:function(){var a=this;this.callParent(arguments);a.titleCmp.childEls.push("subTextEl")},setSubTitle:function(b){var c=this,a=c.titleCmp;c.subtitle=b;if(a.rendered){a.subTextEl.update(c.subtitle||"&#160;");a.updateLayout()}else{c.titleCmp.on({render:function(){c.setSubTitle(b)},single:true})}}});Ext.define("ExtThemeNeptune.toolbar.Toolbar",{override:"Ext.toolbar.Toolbar",usePlainButtons:false,border:false});Ext.define("ExtThemeNeptune.layout.component.Dock",{override:"Ext.layout.component.Dock",noBorderClassTable:[0,Ext.baseCSSPrefix+"noborder-l",Ext.baseCSSPrefix+"noborder-b",Ext.baseCSSPrefix+"noborder-bl",Ext.baseCSSPrefix+"noborder-r",Ext.baseCSSPrefix+"noborder-rl",Ext.baseCSSPrefix+"noborder-rb",Ext.baseCSSPrefix+"noborder-rbl",Ext.baseCSSPrefix+"noborder-t",Ext.baseCSSPrefix+"noborder-tl",Ext.baseCSSPrefix+"noborder-tb",Ext.baseCSSPrefix+"noborder-tbl",Ext.baseCSSPrefix+"noborder-tr",Ext.baseCSSPrefix+"noborder-trl",Ext.baseCSSPrefix+"noborder-trb",Ext.baseCSSPrefix+"noborder-trbl"],edgeMasks:{top:8,right:4,bottom:2,left:1},handleItemBorders:function(){var y=this,f=0,z=8,A=4,l=2,e=1,a=y.owner,s=a.bodyBorder,n=a.border,j=y.collapsed,p=y.edgeMasks,k=y.noBorderClassTable,x=a.dockedItems.generation,w,d,v,h,r,m,u,o,g,q,t,c;if(y.initializedBorders===x){return}t=[];c=[];d=y.getBorderCollapseTable();k=y.getBorderClassTable?y.getBorderClassTable():k;y.initializedBorders=x;y.collapsed=false;v=y.getDockedItems();y.collapsed=j;for(r=0,m=v.length;r<m;r++){u=v[r];if(u.ignoreBorderManagement){continue}o=u.dock;q=h=0;t.length=0;c.length=0;if(o!=="bottom"){if(f&z){w=u.border}else{w=n;if(w!==false){h+=z}}if(w===false){q+=z}}if(o!=="left"){if(f&A){w=u.border}else{w=n;if(w!==false){h+=A}}if(w===false){q+=A}}if(o!=="top"){if(f&l){w=u.border}else{w=n;if(w!==false){h+=l}}if(w===false){q+=l}}if(o!=="right"){if(f&e){w=u.border}else{w=n;if(w!==false){h+=e}}if(w===false){q+=e}}if((g=u.lastBorderMask)!==q){u.lastBorderMask=q;if(g){c[0]=k[g]}if(q){t[0]=k[q]}}if((g=u.lastBorderCollapse)!==h){u.lastBorderCollapse=h;if(g){c[c.length]=d[g]}if(h){t[t.length]=d[h]}}if(c.length){u.removeCls(c)}if(t.length){u.addCls(t)}f|=p[o]}q=h=0;t.length=0;c.length=0;if(f&z){w=s}else{w=n;if(w!==false){h+=z}}if(w===false){q+=z}if(f&A){w=s}else{w=n;if(w!==false){h+=A}}if(w===false){q+=A}if(f&l){w=s}else{w=n;if(w!==false){h+=l}}if(w===false){q+=l}if(f&e){w=s}else{w=n;if(w!==false){h+=e}}if(w===false){q+=e}if((g=y.lastBodyBorderMask)!==q){y.lastBodyBorderMask=q;if(g){c[0]=k[g]}if(q){t[0]=k[q]}}if((g=y.lastBodyBorderCollapse)!==h){y.lastBodyBorderCollapse=h;if(g){c[c.length]=d[g]}if(h){t[t.length]=d[h]}}if(c.length){a.removeBodyCls(c)}if(t.length){a.addBodyCls(t)}},onRemove:function(b){var a=b.lastBorderMask;if(!b.isDestroyed&&!b.ignoreBorderManagement&&a){b.lastBorderMask=0;b.removeCls(this.noBorderClassTable[a])}this.callParent([b])}});Ext.define("ExtThemeNeptune.container.ButtonGroup",{override:"Ext.container.ButtonGroup",usePlainButtons:false});Ext.define("Skyline.form.Labelable",{override:"Ext.form.Labelable",labelAlign:"right",labelPad:15,msgTarget:"under",blankText:"This is a required field"});Ext.define("Skyline.window.MessageBox",{override:"Ext.window.MessageBox",shadow:false,reconfigure:function(a){if(a.ui){this.ui=a.ui}this.callParent(arguments)},initComponent:function(){var a=this,b=a.title;a.title=null;this.callParent(arguments);this.topContainer.padding=0;a.titleComponent=new Ext.panel.Header({title:b});a.promptContainer.insert(0,a.titleComponent)},setTitle:function(d){var b=this,c=b.titleComponent;if(c){var a=c.title}if(c){if(c.isHeader){c.setTitle(d)}else{c.title=d}}else{if(b.rendered){b.updateHeader()}}b.fireEvent("titlechange",b,d,a)}},function(){Ext.MessageBox=Ext.Msg=new this()});Ext.define("Skyline.form.field.Text",{override:"Ext.form.field.Text",labelAlign:"right",labelPad:15,msgTarget:"under",blankText:"This is a required field"});Ext.define("Skyline.form.field.Base",{override:"Ext.form.field.Base",labelAlign:"right",labelPad:15,msgTarget:"under",blankText:"This is a required field"});Ext.define("Skyline.form.Label",{override:"Ext.form.Label",cls:"x-form-item-label"});Ext.define("ExtThemeNeptune.toolbar.Paging",{override:"Ext.toolbar.Paging",defaultButtonUI:"plain-toolbar",inputItemWidth:40});Ext.define("ExtThemeNeptune.picker.Month",{override:"Ext.picker.Month",measureMaxHeight:36});Ext.define("ExtThemeNeptune.form.field.HtmlEditor",{override:"Ext.form.field.HtmlEditor",defaultButtonUI:"plain-toolbar"});Ext.define("Skyline.grid.Panel",{override:"Ext.grid.Panel",bodyBorder:true,enableColumnHide:false,enableColumnMove:false,enableColumnResize:false});Ext.define("Skyline.view.Table",{override:"Ext.view.Table",bodyBorder:true});Ext.define("ExtThemeNeptune.panel.Table",{override:"Ext.panel.Table",bodyBorder:true});Ext.define("ExtThemeNeptune.grid.RowEditor",{override:"Ext.grid.RowEditor",buttonUI:"default-toolbar"});Ext.define("ExtThemeNeptune.grid.column.RowNumberer",{override:"Ext.grid.column.RowNumberer",width:25});Ext.define("ExtThemeNeptune.resizer.Splitter",{override:"Ext.resizer.Splitter",size:8});Ext.define("ExtThemeNeptune.menu.Menu",{override:"Ext.menu.Menu",showSeparator:false});Ext.define("ExtThemeNeptune.menu.Separator",{override:"Ext.menu.Separator",border:true});Ext.define("ExtThemeNeptune.panel.Tool",{override:"Ext.panel.Tool",height:16,width:16});Ext.define("ExtThemeNeptune.tab.Tab",{override:"Ext.tab.Tab",border:false});Ext.define("Skyline.button.TagButton",{extend:"Ext.button.Split",alias:"widget.tag-button",split:true,menu:{},ui:"tag",arrowCls:null,afterRender:function(){var a=this,c=a.getEl().first(),d=c.first().first(),b=c.createChild({tag:"span",cls:"x-btn-tag-right"}),e=c.getById(b.id);d.addCls(a.iconCls?"x-btn-tag-text":"x-btn-tag-text-noicon");e.on("click",function(){a.fireEvent("closeclick",a);a.destroy()});this.callParent(arguments)}});Ext.define("Skyline.button.SortItemButton",{extend:"Skyline.button.TagButton",alias:"widget.sort-item-btn",name:"sortitembtn",iconCls:"x-btn-sort-item-asc",sortOrder:"asc"});Ext.define("Skyline.button.StepButton",{extend:"Ext.button.Button",alias:"widget.step-button",ui:"step-active"});Ext.define("Skyline.menu.NavigationItem",{extend:"Ext.menu.Item",alias:"widget.navigation-item",arrowCls:null,renderTpl:['<tpl if="plain">',"{text}","<tpl else>",'<a id="{id}-itemEl"',' class="'+Ext.baseCSSPrefix+'menu-item-link{childElCls}"',' href="{href}"','<tpl if="hrefTarget"> target="{hrefTarget}"</tpl>',' hidefocus="true"',' unselectable="on"','<tpl if="tabIndex">',' tabIndex="{tabIndex}"',"</tpl>",">",'<div role="img" id="{id}-iconEl" class="'+Ext.baseCSSPrefix+"menu-item-icon {iconCls}",'{childElCls} {glyphCls}" style="<tpl if="icon">background-image:url({icon});</tpl>','<tpl if="glyph && glyphFontFamily">font-family:{glyphFontFamily};</tpl>">','<tpl if="glyph">&#{glyph};</tpl>',"</div>",'<span class="navigation-item-number">{index}</span>','<span id="{id}-textEl" class="'+Ext.baseCSSPrefix+'menu-item-text" unselectable="on">{text}</span>',"</a>","</tpl>"]});Ext.define("Skyline.menu.NavigationMenu",{extend:"Ext.menu.Menu",alias:"widget.navigation-menu",cls:"x-navigation-menu",defaults:{xtype:"navigation-item"},floating:false,hidden:false,activeStep:1,jumpBack:true,jumpForward:false,listeners:{add:function(c,b,a){b.renderData.index=b.index=++a;this.updateItemCls(a)},click:function(b,a){a.index<b.activeStep?(b.jumpBack?b.moveTo(a.index):null):(b.jumpForward?b.moveTo(a.index):null)}},updateItemCls:function(a){var c=this,b=c.items.getAt(a-1);b.removeCls(["step-completed","step-active","step-non-completed"]);a<c.activeStep?b.addCls("step-completed"):(a>c.activeStep?b.addCls("step-non-completed"):b.addCls("step-active"))},moveTo:function(b){var a=this;a.moveToStep(b);a.fireEvent("movetostep",a.activeStep)},moveToStep:function(b){var a=this,c=a.items.getCount();if(1<b<c){a.activeStep=b;a.items.each(function(e){var d=e.index;a.updateItemCls(d)})}},getActiveStep:function(){return this.activeStep},moveNextStep:function(){this.moveToStep(this.activeStep+1)},movePrevStep:function(){this.moveToStep(this.activeStep-1)}});Ext.define("Skyline.panel.FilterToolbar",{extend:"Ext.panel.Panel",alias:"widget.filter-toolbar",titlePosition:"left",layout:{type:"hbox",align:"stretch"},header:false,ui:"filter-toolbar",showClearButton:true,items:[{xtype:"container",itemId:"itemsContainer",layout:{type:"hbox",align:"stretch"},items:[]},{xtype:"label",itemId:"emptyLabel",hidden:true},{xtype:"container",itemId:"toolsContainer",layout:{type:"hbox",align:"stretch"},dock:"left"}],dockedItems:[{xtype:"header",dock:"left"},{xtype:"button",text:"Clear all",action:"clear",disabled:true,dock:"right"}],updateContainer:function(a){var b=a.items.getCount();!b?this.getEmptyLabel().show():this.getEmptyLabel().hide();this.getClearButton().setDisabled(!b)},initComponent:function(){var a=this;this.dockedItems[0].title=a.title;this.items[0].items=a.content;this.items[1].text=a.emptyText;this.items[2].items=a.tools;this.callParent(arguments);if(!this.showClearButton){this.getClearButton().hide()}this.getContainer().on("afterlayout","updateContainer",this)},getContainer:function(){return this.down("#itemsContainer")},getTools:function(){return this.down("#toolsContainer")},getClearButton:function(){return this.down('button[action="clear"]')},getEmptyLabel:function(){return this.down("#emptyLabel")}});Ext.define("Skyline.panel.StepPanel",{extend:"Ext.panel.Panel",alias:"widget.step-panel",text:"Some step text",indexText:"12",index:null,isLastItem:null,isFirstItem:null,isMiddleItem:null,isOneItem:null,isActiveStep:null,isCompletedStep:null,isNonCompletedStep:null,state:"noncompleted",layout:{type:"vbox",align:"left"},states:{active:["step-active","step-label-active"],completed:["step-completed","step-label-completed"],noncompleted:["step-non-completed","step-label-non-completed"]},items:[],handler:function(){},getStepDots:function(){return{layout:{type:"vbox",align:"left"},cls:"x-panel-step-dots",items:[{xtype:"box",name:"bottomdots",cls:"x-image-step-dots"}]}},getStepLabel:function(){var a=this;return{name:"step-label-side",layout:{type:"hbox",align:"middle"},items:[{xtype:"button",name:"steppanellabel",text:a.text,cls:"x-label-step",ui:"step-label-active",handler:a.handler}]}},getStepPanelLayout:function(){var a=this;return{name:"basepanel",layout:{type:"hbox",align:"middle"},items:[{name:"steppanelbutton",xtype:"step-button",ui:"step-active",text:a.indexText,handler:a.handler},a.getStepLabel()]}},doStepLayout:function(){var b=this,a=null;b.isFirstItem&&(a=[b.getStepPanelLayout(),b.getStepDots()]);b.isLastItem&&(a=[b.getStepDots(),b.getStepPanelLayout()]);b.isMiddleItem&&(a=[b.getStepDots(),b.getStepPanelLayout(),b.getStepDots()]);b.isOneItem&&(a=[b.getStepPanelLayout()]);b.items=a},afterRender:function(a){a.stepButton=this.down("panel[name=basepanel]");a.stepLabel=this.down();console.log(this.stepButton,this.stepLabel)},setState:function(a){!a&&(this.state=a);console.log(this,this.stepButton,this.stepLabel);this.stepButton.setUI(this.states[this.state][0]);this.stepLabel.setUI(this.states[this.state][1])},getState:function(){return this.state},initComponent:function(){var a=this;a.doStepLayout();a.callParent(arguments)}});Ext.define("Skyline.ux.window.Notification",{override:"Ext.ux.window.Notification",title:false,position:"t",stickOnClick:false,closable:false,ui:"notification"});