Ext.require([
	'Presage2.view.ColouredTrlsVisualiser'
]);

Ext.define('Presage2.view.ColouredTrlsPlugin', {
	extend: 'Presage2.view.VisualiserPlugin',
	alias: 'widget.colouredtrlsplugin',
	initComponent: function() {
		Ext.apply(this, {
			drawPanel: 'Presage2.view.ColouredTrlsVisualiser'
		});
		this.callParent(arguments);

		var pmodel = Ext.define('Property', {
			extend: 'Ext.data.Model',
			fields: [
				{name: 'key', type: 'string'},
				{name: 'value', type: 'string'}
			]
		});
		this.propertyData = {
			key: '',
			value: '',
			children: [],
			expanded: true
		};

		this.propertyStore = Ext.create('Ext.data.TreeStore', {
			model: pmodel,
			root: this.propertyData
		});

		this.properties = Ext.create('Ext.tree.Panel', {
			title: 'Agent properties',
			margin: 10,
			width: 400,
			height: 400,
			store: this.propertyStore,
			rootVisible: false,
			columns: [
				{
					xtype: 'treecolumn',
					flex: 1
				},
				{
					text: "Key",
					dataIndex: 'key',
					flex: 2
				},{
					text: "Value",
					dataIndex: 'value',
					flex: 4
				}
			]
		});
		this.sidemenu.add(this.properties);

		this.addListener('setTime', this.updateAgentProperties, this);
	},
	onInitialLoad : function() {
		this.callParent(arguments);
		this.propertyStore.getRootNode().removeAll();
		this.updateAgentProperties(0);
	},
	updateAgentProperties: function(newTime) {
		if(this.sim != undefined) {
			var timeline = this.sim.timeline(),
				step = timeline.getById(newTime);
			if(step != null) {
				var rootNode = this.propertyStore.getRootNode();
				if(rootNode.hasChildNodes()) {
					// update nodes
					var agentNodes = {};
					rootNode.eachChild(function(node) {
						agentNodes[node.data.value] = node;
					}, this);
					step.agents().each(function(ag) {
						if(ag.getId() in agentNodes) {
							// node exists for this agent
							var node = agentNodes[ag.getId()],
								props = ag.data.data;
							node.eachChild(function(prop) {
								if(prop.get('key') in props) {
									prop.set('value', props[prop.data.key]);
								}
							}, this);
						} else {
							this.insertAgentProperties(ag);
						}
					}, this);
				} else {
					// create nodes
					step.agents().each(this.insertAgentProperties, this);
				}
			}
		}
	},
	insertAgentProperties: function(ag) {
		var rootNode = this.propertyStore.getRootNode();
		if(ag.getId() in this.drawPanel.agentNames) {
			var key = this.drawPanel.agentNames[ag.getId()],
				props = ag.data.data,
				node = Ext.create('Property', {
					key: key,
					value: ag.getId()
				});
			node = rootNode.appendChild(node);
			for(var p in props) {
				node.appendChild({
					key: p,
					value: props[p],
					leaf: true
				});
			}
		}
	}
});
