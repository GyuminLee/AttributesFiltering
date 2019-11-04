package org.processmining.attributebasedfiltering.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Visualize AbF",
	parameterLabels = {"Visualizing Filtered Log"},
	returnLabels = {"JPanel"},
	returnTypes = {JPanel.class})

@Visualizer
public class AbFVisualizerPlugin {
	@PluginVariant(requiredParameterLabels = {0})
	public static JPanel visualize (PluginContext context, AbFModel model) {
		
		return new MainView(context, model);
	}
}


class MainView extends JPanel {
	PluginContext context;
	AbFModel model;
	JCheckBox[] attrCheckBox;
	JLabel attrLabel;
	ExportButtonListener exportCleanButtonListener;
	ExportButtonListener exportNoiseButtonListener;
	ExportButtonListener exportLabelButtonListener;


	ArrayList<String> selectedAttribute = new ArrayList<String>();
	double waitingTimeValue = 0.1;
	double thresholdValue = 0.1;
	double dfrValue = 0.1;
	
	private NiceDoubleSlider waitingTimeSlider = SlickerFactory.instance()
			.createNiceDoubleSlider("Waiting Time Threshold", 0, 1, waitingTimeValue, Orientation.HORIZONTAL);
	
	private NiceDoubleSlider dfrSlider = SlickerFactory.instance()
			.createNiceDoubleSlider("DFR Threshold", 0, 1, dfrValue, Orientation.HORIZONTAL);
	
	
	private NiceDoubleSlider thresholdSlider = SlickerFactory.instance()
			.createNiceDoubleSlider("Attributes Threshold", 0, 1, thresholdValue, Orientation.HORIZONTAL);
	
	
	public MainView(PluginContext context, AbFModel model) {
		this.context = context;
		this.model = model;
		JPanel attrPanel = new JPanel();
		attrPanel.setLayout(new BoxLayout(attrPanel, BoxLayout.Y_AXIS));
		ArrayList<String> attrList = new ArrayList<String>(model.getAttributeSet());
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		this.setLayout(rl);
		
		attrLabel = new JLabel("Attribute list ");
		attrPanel.add(attrLabel);

		attrCheckBox = new JCheckBox[attrList.size()];
		for(int i = 0; i < attrList.size(); i++) {
			attrCheckBox[i] = new JCheckBox(attrList.get(i));
			attrPanel.add(attrCheckBox[i]);

		}
		
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(attrPanel);
		
		this.add(scrollPanel);
		JLabel sliderLabel = new JLabel("Threshold Slider");
		this.add(sliderLabel);
		this.add(waitingTimeSlider);
		this.add(dfrSlider);
		this.add(thresholdSlider);
		
		JLabel buttonLabel = new JLabel("Export Log");
		JButton addLabelBtn = new JButton("Add the noise label");
		JButton exportNoiseBtn = new JButton("Export Noise Log");
		JButton exportCleanBtn = new JButton("Export Clean Log");
		exportLabelButtonListener = new ExportButtonListener(context, model.inputLog, attrCheckBox, thresholdSlider, waitingTimeSlider, dfrSlider, 0);
		exportNoiseButtonListener = new ExportButtonListener(context, model.inputLog, attrCheckBox, thresholdSlider, waitingTimeSlider, dfrSlider, 1);
		exportCleanButtonListener = new ExportButtonListener(context, model.inputLog, attrCheckBox, thresholdSlider, waitingTimeSlider, dfrSlider, 2);

		addLabelBtn.addActionListener(exportLabelButtonListener);
		exportNoiseBtn.addActionListener(exportNoiseButtonListener);
		exportCleanBtn.addActionListener(exportCleanButtonListener);
		
		this.add(buttonLabel);
		this.add(addLabelBtn);
		this.add(exportNoiseBtn);
		this.add(exportCleanBtn);
	}

}

class ExportButtonListener implements ActionListener {

	PluginContext context;
	XLog originalLog;
	
	JCheckBox[] attrCheckBox;
	ArrayList<String> selectedAttribute;
	NiceDoubleSlider thresholdSlider;
	NiceDoubleSlider waitingTimeSlider;
	NiceDoubleSlider dfrSlider;
	
	MainView mainView;
	int filteringType; 
	
	public ExportButtonListener(PluginContext context, XLog originalLog, JCheckBox[] attrCheckBox, NiceDoubleSlider thresholdSlider,
	NiceDoubleSlider waitingTimeSlider, NiceDoubleSlider dfrSlider, int filteringType) {
		this.context = context;
		this.originalLog = originalLog;
		this.attrCheckBox = attrCheckBox;
		this.thresholdSlider = thresholdSlider;
		this.waitingTimeSlider = waitingTimeSlider;
		this.dfrSlider = dfrSlider;
		this.filteringType = filteringType;
		
	}
	
	public void actionPerformed(ActionEvent e) {
		selectedAttribute = new ArrayList<>();
		for(int i = 0; i < attrCheckBox.length; i++) {
			if(attrCheckBox[i].isSelected()) {
				selectedAttribute.add(attrCheckBox[i].getText());
			}
		}
		XLog outputLog = AbFAlgorithm.doFiltering(originalLog, selectedAttribute, thresholdSlider.getValue(), waitingTimeSlider.getValue(), dfrSlider.getValue(), filteringType);
		System.out.println("Export Click!" + filteringType);
		System.out.println("thresholdValue : " + thresholdSlider.getValue());
		System.out.println("waitingTimeValue : " + waitingTimeSlider.getValue());
		System.out.println("dfrValue : "  + dfrSlider.getValue());
		System.out.println("selectedAttr : " + selectedAttribute);
		String outputName = "";
		if(filteringType == 0) { // Label
			outputName = "Labeled Log";
		} else if(filteringType == 1) { // Noise
			outputName = "Noise Log";
		} else if(filteringType == 2) { // Clean
			outputName = "Clean Log";
		}
		context.getProvidedObjectManager().createProvidedObject(outputName, outputLog, XLog.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(outputLog)
			.setFavorite(true);
		}
		
	}
	
}