package org.processmining.attributesfiltering.verify;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Noise Counter", parameterLabels = {"Event Log", "Parameter Object"}, 
returnLabels = {"Filtered log"}, returnTypes = {XLog.class})


public class NoiseCounterPlugin {
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Gyumin Lee ", email = "gyumin.lee@rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog run(UIPluginContext context, XLog log) {

		XAttribute  nn = new XAttributeLiteralImpl("output", "dirty");
		int noiseCnt = 0;

		for(XTrace trace : log) {
			if(trace.getAttributes().get("output").equals(nn)) {
				noiseCnt++;
			}
		}
		
		System.out.println("Noise Count : " + noiseCnt);
		return log;
	}
}

