package org.processmining.automatedfiltering.plugin;


import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Attributes Filtering", parameterLabels = {"Event Log", "Parameter Object"}, 
returnLabels = {"Filtered log"}, returnTypes = {XLog.class})


public class AFPlugin {
	
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Gyumin Lee ", email = "gyumin.lee@rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })

	public XLog run(UIPluginContext context, XLog log) {
		
		AFDialog dialog = new AFDialog(log);
		InteractionResult result = context.showWizard("Select Attributes", true, true, dialog);
		return run(context, log, dialog.getSelectedAttributes(), dialog.getThresholdValue(), dialog.getWaitingTimeValue(), dialog.getDfrValue());
	}
	
	
	@PluginVariant(requiredParameterLabels = {0, 1})
	public static XLog run(PluginContext context, XLog log, List<String> selectedParam, double thresholdValue, double waitingTimeValue, double dfrValue) {
		System.out.println("Do Filter Func Start");
		
		return AFAlgorithm.doFiltering(log, selectedParam, thresholdValue, waitingTimeValue, dfrValue);
	}

	
}