package org.processmining.automatedfiltering.plugin;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.widgets.ProMList;

import com.fluxicon.slickerbox.colors.SlickerColors;
import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayoutConstraints;

public class AFDialog extends JPanel{

	private Map<String, ProMList<String>> attributesMap;
	private ProMList<String> selectedAttributes;
	
	private NiceDoubleSlider waitingTimeSlider = SlickerFactory.instance()
			.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.1, Orientation.HORIZONTAL);
	
	private NiceDoubleSlider dfrSlider = SlickerFactory.instance()
			.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.1, Orientation.HORIZONTAL);
	
	
	
	private NiceDoubleSlider thresholdSlider = SlickerFactory.instance()
			.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.1, Orientation.HORIZONTAL);
	
	public AFDialog(XLog log) {
		
		attributesMap = new HashMap<String, ProMList<String>>();
		
		
		double size[][] = { { TableLayoutConstraints.FILL }, { 30, TableLayoutConstraints.FILL } };
		GridLayout layout = new GridLayout(0, 1);
		setLayout(layout);
//		setLayout(new TableLayout(size));
		add(SlickerFactory.instance().createLabel("<html><h2>Select Attribute to be considered</h2>"), "0,0");
		
		selectedAttributes = getAttributesFromLog(log);
		
		attributesMap.put("CaseAttributes", selectedAttributes);
		
		selectedAttributes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JScrollPane attrScrollPane = new JScrollPane();
		SlickerDecorator.instance().decorate(attrScrollPane, SlickerColors.COLOR_BG_3, SlickerColors.COLOR_FG,
				SlickerColors.COLOR_BG_1);
		attrScrollPane.setPreferredSize(new Dimension(250, 300));
		attrScrollPane.setViewportView(selectedAttributes);
		add(waitingTimeSlider);
		add(dfrSlider);
		add(thresholdSlider);
		add(attrScrollPane, "0, 1");
		
		
		
	}
	
	public double getThresholdValue() {
		return thresholdSlider.getValue();
	}
	public double getWaitingTimeValue() {
		return waitingTimeSlider.getValue();
	}
	public double getDfrValue() {
		return dfrSlider.getValue();
	}
	public List getSelectedAttributes() {
		return attributesMap.get("CaseAttributes").getSelectedValuesList();
	}
	
	private ProMList<String> getAttributesFromLog(XLog log) {
		Set<String> attributeSet = new HashSet<String>();
		DefaultListModel<String> listModel = new DefaultListModel<String>();

		
		//Get all trace attributes
		for(XTrace trace : log) {
			for(String str : trace.getAttributes().keySet()) {
				attributeSet.add(str);
			}
		}
		
		for(String str : attributeSet) {
			listModel.addElement(str);
		}
		
		ProMList<String> attrList = new ProMList<>("Select Attributes to be considered", listModel);
		
		return attrList;
	}
	
}
