package org.confetti.xml.core.time;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Bubla Gabor
 */
@XmlRootElement
@XmlType(propOrder = {
		"weight", 
		"teacherName", "intervalStartHour", "intervalEndHour", "maxDaysPerWeek", 
		"active", "comment"})
public class ConstraintTeacherIntervalMaxDaysPerWeek extends TimeConstraint {
	@XmlElement(name = "Teacher_Name") private String teacherName;
	@XmlElement(name = "Interval_Start_Hour") private String intervalStartHour;
	@XmlElement(name = "Interval_End_Hour") private String intervalEndHour;
	@XmlElement(name = "Max_Days_Per_Week") private int maxDaysPerWeek;
}
