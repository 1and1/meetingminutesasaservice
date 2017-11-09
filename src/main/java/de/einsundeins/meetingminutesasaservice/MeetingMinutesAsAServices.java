/*
 * Copyright 2017 1&1 Internet SE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
