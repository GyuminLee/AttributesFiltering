package org.processmining.attrubutesfiltering.plugin;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class AFAlgorithm {

	public static XLog doFiltering(XLog originalLog, List<String> selectedAttribute, double thresholdValue, double waitingTimeValue, double dfrValue) {

		long startMillis = System.currentTimeMillis();
		
		System.out.println("Threshold : " + thresholdValue);
		System.out.println("Selected Attribute :" );
		for(String str : selectedAttribute) {
			System.out.println(str);
		}
		XLog log = (XLog)originalLog.clone();


		Map<String, Integer> generalCountingMap = new HashMap<>();

		//Map for selected Attributes
		Map<String, Map<String, Integer>> attributesCountingMapMap = new HashMap<>();
		for(String selectedAttrStr : selectedAttribute) {
			Map<String, Integer> countingMap = new HashMap<>();
			attributesCountingMapMap.put(selectedAttrStr, countingMap);
		}
		double probThresholdDfr = dfrValue; 
		double probThresholdWaitingTime = waitingTimeValue;

		Map<String, Integer> eventCountingMap = new HashMap<String, Integer>();

		Map<String, Integer> directFollowMap = new HashMap<String, Integer>();
		Map<String, Integer> prevEventMap = new HashMap<String, Integer>();
		Map<String, Integer> nextEventMap = new HashMap<String, Integer>();

		Map<String, List<Double>> waitingTimeMap = new HashMap<String, List<Double>>();
		Map<String, Double> activityOutlierPercentageMap = new HashMap<String, Double>();


		for(XTrace trace : log) {
			String eventList = "";
			int counter = 0;
			String prevTime = "";
			String nextTime = "";

			for(XEvent event : trace) {
				String eventName = event.getAttributes().get("concept:name").toString();

				String separator = "=>>";

				//CountingKeyMap
				for(String str : selectedAttribute) {
					String keyStr = eventName + separator + str;

					if(generalCountingMap.containsKey(keyStr)) {
						generalCountingMap.put(keyStr, generalCountingMap.get(keyStr) + 1);
					} else {
						generalCountingMap.put(keyStr, 1);
					}

				}

				eventList += eventName + ">>";

				//Building waiting time map
				if(counter == 0) {
					prevTime = event.getAttributes().get("time:timestamp").toString();

				} else {
					prevTime = nextTime;
				}

				counter++;

				nextTime = event.getAttributes().get("time:timestamp").toString();

				OffsetDateTime nextODT = OffsetDateTime.parse(nextTime);
				OffsetDateTime prevODT = OffsetDateTime.parse(prevTime);

				double diffTime = Duration.between(prevODT, nextODT).getSeconds();

				if(waitingTimeMap.containsKey(eventName)) {
					List<Double> t = waitingTimeMap.get(eventName);
					t.add(diffTime);
					waitingTimeMap.put(eventName, t);
				} else {
					List<Double> t = new ArrayList<Double>();
					t.add(diffTime);
					waitingTimeMap.put(eventName, t);
				}

				//Counting
				if(eventCountingMap.containsKey(eventName)) {
					eventCountingMap.put(eventName, eventCountingMap.get(eventName) + 1);
				} else {
					eventCountingMap.put(eventName, 1);
				}
			}

			//Building DFR
			String[] eventArray = eventList.split(">>");
			for(int i = 0; i < eventArray.length - 1; i++) {
				String prevEvent = eventArray[i];
				String nextEvent = eventArray[i+1];
				String directFollowRelation = prevEvent + ">>" + nextEvent;

				if(directFollowMap.containsKey(directFollowRelation)) {
					directFollowMap.put(directFollowRelation, directFollowMap.get(directFollowRelation) + 1);
				} else {
					directFollowMap.put(directFollowRelation, 1);
				}

				if(prevEventMap.containsKey(prevEvent)) {
					prevEventMap.put(prevEvent, prevEventMap.get(prevEvent) + 1);
				} else {
					prevEventMap.put(prevEvent, 1);
				}

				if(nextEventMap.containsKey(nextEvent)) {
					nextEventMap.put(nextEvent, nextEventMap.get(nextEvent) + 1);
				} else {
					nextEventMap.put(nextEvent, 1);
				}
			}
		}

		//Find noises 
		Set<String> ActivitiesSet = waitingTimeMap.keySet();
		String[] ActivitiesArray = ActivitiesSet.toArray(new String[ActivitiesSet.size()]); 

		//find the oulier in execution times

		for (int i = 0; i < ActivitiesArray.length; i++) {
			List<Double> tempExecutiontime = waitingTimeMap.get(ActivitiesArray[i]);
			Collections.sort(tempExecutiontime);
			List<Double> sd = getOutliers(tempExecutiontime);
			double ss = sd.size()*1.0/tempExecutiontime.size();	
			activityOutlierPercentageMap.put(ActivitiesArray[i], ss);
		}

		Set<Integer> removeSet = new HashSet<Integer>();

		for(XTrace trace : log) {
			String eventList = "";
			boolean isNegativeOutlier = false;
			for(XEvent event : trace) {
				String eventName = event.getAttributes().get("concept:name").toString();
				eventList += eventName + ">>";
			}
			String[] eventArray = eventList.split(">>");
			String separator = "=>>";

			for(int i = 0; i < eventArray.length - 1; i++) {
				String prevEvent = eventArray[i];
				String nextEvent = eventArray[i+1];
				String directFollowRelation = prevEvent + ">>" + nextEvent;

				int eventCountNum = eventCountingMap.get(nextEvent);

				if(activityOutlierPercentageMap.get(eventArray[i+1]) > probThresholdWaitingTime
						&& directFollowMap.get(directFollowRelation) * 1.0 / prevEventMap.get(prevEvent) < probThresholdDfr) {
					isNegativeOutlier = true;
					for(String str : selectedAttribute) {
						String keyStr = nextEvent + separator + str;
						int countNum = generalCountingMap.get(keyStr);
						if(countNum * 1.0 / eventCountNum > thresholdValue) {
							isNegativeOutlier = true;
							System.out.println("Undesired behavior (" + keyStr + " )" + (countNum * 1.0 / eventCountNum));
						} else {
							System.out.println("Desired behavior (" + keyStr + " )" + (countNum * 1.0 / eventCountNum));
							isNegativeOutlier = false;
						}
					}
				}
			}
			if(isNegativeOutlier) {
				removeSet.add(log.indexOf(trace));
			}
		}

		ArrayList<Integer> removeList = new ArrayList<Integer>(removeSet);
		Collections.sort(removeList, Collections.reverseOrder());
		System.out.println("The number of undesired behavior : " + removeList.size());
		for(int i = 0; i < removeList.size(); i++) {
			log.remove(Integer.parseInt(removeList.get(i).toString()));
		}

		long endMillis = System.currentTimeMillis();
		
		long executionTime = endMillis - startMillis;
		System.out.println("ExecutionTime : " + executionTime);
		
		return log;
	}


	public static List<Double> getOutliers(List<Double> input) {
		List<Double> output = new ArrayList<Double>();
		List<Double> data1 = new ArrayList<Double>();
		List<Double> data2 = new ArrayList<Double>();
		if (input.size() % 2 == 0) {
			data1 = input.subList(0, input.size() / 2);
			data2 = input.subList(input.size() / 2, input.size());
		} else {
			data1 = input.subList(0, input.size() / 2);
			data2 = input.subList(input.size() / 2 + 1, input.size());
		}
		if (input.size()==1) {
			return data1;
		}
		double q1 = getMedian(data1);
		double q3 = getMedian(data2);
		double iqr = q3 - q1;
		double lowerFence = q1 - 1.5 * iqr;
		double upperFence = q3 + 1.5 * iqr;
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i) < lowerFence || input.get(i) > upperFence)
				output.add(input.get(i));
		}
		return output;
	}

	private static double getMedian(List<Double> data) {
		if (data.size() % 2 == 0)
			return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
		else
			return data.get(data.size() / 2);
	}

}
