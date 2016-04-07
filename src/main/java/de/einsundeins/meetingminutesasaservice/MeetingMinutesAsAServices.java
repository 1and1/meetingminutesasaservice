/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.einsundeins.meetingminutesasaservice;

import de.einsundeins.mmaas.xml.AttendeeType;
import de.einsundeins.mmaas.xml.MeetingMinutesAsAService;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for {@link MeetingMinutesAsAService}.
 * @author stephan
 */
public class MeetingMinutesAsAServices {
	private MeetingMinutesAsAServices() {
		// no construction
	}
	
	/** Finds an attendee by its id.
	 * @param id the id to find the attendee for.
	 * @param minutesAsAService the XML config to look for the id.
	 * @return the found attendee id.
	 */
	public static Optional<AttendeeType> findById(String id, MeetingMinutesAsAService minutesAsAService) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(minutesAsAService);
		return minutesAsAService.getPossibleAttendees().getAttendee().stream().filter(att -> att.getId().equals(id)).findFirst();
	}		
	
	/** Extract the list of possible attendee ids from the XML config.
	 * @param minutesAsAService the config to extract from.
	 * @return the set of extracted ids.
	 */
	public static Set<String> listPossibleAttendeeIds(MeetingMinutesAsAService minutesAsAService) {
		Set<String> result = minutesAsAService.getPossibleAttendees().getAttendee().stream().filter(att -> att.isEnabled()).map(att -> att.getId()).collect(Collectors.toSet());
		return result;
	}	
}
