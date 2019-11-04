package org.processmining.attributebasedfiltering.plugin;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;

public class AbFModel {

	PluginContext context;
	XLog inputLog;
	XLog outputLog;
	
	Set<String> attributeSet;
	
	
	public AbFModel(PluginContext context, XLog inputLog) {
		this.context = context;
		this.inputLog = inputLog;
		this.attributeSet = new HashSet<String>();
		
		//Get all trace attributes
		for(XTrace trace : inputLog) {
			for(String str : trace.getAttributes().keySet()) {
				attributeSet.add(str);
			}
		}
		
		
	}


	public Set<String> getAttributeSet() {
		return attributeSet;
	}


	public void setAttributeSet(Set<String> attributeSet) {
		this.attributeSet = attributeSet;
	}
	
	
}
	