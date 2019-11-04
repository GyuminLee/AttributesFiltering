package org.processmining.attributebasedfiltering.plugin;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Attribute-based Filtering",
parameterLabels = {"Log"} ,
returnLabels = {"Attribute-based Filtering Model"},
returnTypes = {AbFModel.class})

public class AbFPlugin {
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Gyumin Lee",
			email = "gyumin.lee@rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public AbFModel createInteractvieView(PluginContext context, XLog log) {
		System.out.println("create Plugin View Func Start");
		context.getProgress().setIndeterminate(true);

		return new AbFModel(context, log);
	}
}
