package org.confetti.xml.core.space;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Bubla Gabor
 */
@XmlRootElement
@XmlType(propOrder = {
		"weight", 
		"teacher", "nrOfPreferredRooms", "rooms", 
		"active", "comment"})
public class ConstraintTeacherHomeRooms extends SpaceConstraint {
	@XmlElement(name = "Teacher") private String teacher;
	@XmlElement(name = "Number_of_Preferred_Rooms") private int nrOfPreferredRooms;
	@XmlElement(name = "Preferred_Room") private List<String> rooms;
}
