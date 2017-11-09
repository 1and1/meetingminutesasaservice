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
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stephan
 */
public class ConsoleUI {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleUI.class);
	
	private Console console;
	
	private BufferedReader bufferedReader;
	
	public ConsoleUI() {
		console = System.console();
		if (console == null) {
			bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		}
	}
	
	private String readLine() throws IOException {
		if (console != null) {
			return console.readLine();
		} else
			return bufferedReader.readLine();
	}
	
	/** Asks for acceptance on the console. */
	public boolean askAccept(AttendeeType choice) throws IOException {
		boolean result = false;
		// show the winner
		System.out.printf("The winner is: %s\n", choice.getName());
		System.out.printf("Accept? ");
		System.out.flush();
		String line = readLine();
		if (line.matches("(j|ja|y|yes|t|true)")) {
			result = true;
		}
		return result;
	}
			
	/** Selects non available attendees using console interaction. */
	public Set<String> knockOutAwayAttendeeIds(Collection<String> attendeeIds, MeetingMinutesAsAService minutesAsAService) throws IOException {
		List<String> result = new ArrayList<>(attendeeIds);
		boolean finished = false;
		
		System.out.printf("List removal of non-present people\n");
		System.out.printf("==================================\n");
		do {
			for (String attId : result) {
				Optional<AttendeeType> attendee = MeetingMinutesAsAServices.findById(attId, minutesAsAService);
				System.out.printf("%d: %s (%s, %d)\n", result.indexOf(attId), attId, attendee.get().getName(), attendee.get().getCount());
			}
			System.out.printf("> ");
			System.out.flush();
			String line = readLine();
			if (line.equals("")) {
				finished = true;
				continue;
			}
			try {
				Pattern p = Pattern.compile("([0-9]+)");
				Matcher m = p.matcher(line);
				List<String> removeUs = new ArrayList<>();
				while (m.find()) {
					int index = Integer.parseInt(m.group(1));
					removeUs.add(result.get(index));
				}
				result.removeAll(removeUs);
			}
			catch (NumberFormatException e) {
				LOGGER.debug("NFE for "+line, e);
			}
		} while (!finished);
		return new HashSet<>(result);
	}
	
}
