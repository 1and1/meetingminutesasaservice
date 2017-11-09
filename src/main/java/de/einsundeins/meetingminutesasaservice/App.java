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
import de.einsundeins.mmaas.xml.LogType;
import de.einsundeins.mmaas.xml.MeetingMinutesAsAService;
import de.einsundeins.mmaas.xml.ObjectFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Fair meeting minute scheduling tool.
 */
public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	
	/** Command line parameters. */
	private final Parameters parameters;

	/** Shared JAXB context for reading and writing {@link MeetingMinutesAsAService}. */
	private final JAXBContext jaxbc;
	
	/** The current XML in an objectified version.
	 * This object is altered by almost all of this class.
	 */
	private MeetingMinutesAsAService minutesAsAService;
	
	/** The pseudo random generator to use. */
	private final Random random;
	
	private final ConsoleUI ui;
	
	public App(Parameters parameters) throws JAXBException {
		this.parameters = Objects.requireNonNull(parameters);
		this.jaxbc = JAXBContext.newInstance(MeetingMinutesAsAService.class);
		this.random = new Random();
		this.ui = new ConsoleUI();
	}

	/** Where to search for the path of the XML schema. 
	 * TODO this is pfui
	 */
	private final static String SCHEMA_LOCATIONS[] = {"/mmaas.xsd","/META-INF/mmaas.xsd", "/config/mmaas.xsd", "/src/main/xsd/mmaas.xsd"};

	/** Guess the location of the XML schema.
	 * @see #SCHEMA_LOCATIONS
	 */
	private static URL guessSchemaLocation() {
		Optional<URL> url = Arrays.asList(SCHEMA_LOCATIONS).stream().
				map(location -> App.class.getResource(location)).filter(u -> u != null).findFirst();
		
		url.ifPresent(u ->
			LOGGER.debug("Found resource at {}", u.toExternalForm())
		);
		return url.get();
	}
	
	/** Reads the XML config. 
	 * @see #write(de.einsundeins.mmaas.xml.MeetingMinutesAsAService) 
	 */
	private void read() throws IOException {
		try {
			LOGGER.debug("Reading");
			Unmarshaller unmarshaller = jaxbc.createUnmarshaller();
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			LOGGER.debug("Reading schema file from {}", guessSchemaLocation());
			Schema schema = schemaFactory.newSchema(guessSchemaLocation());
			unmarshaller.setSchema(schema);
			MeetingMinutesAsAService result = (MeetingMinutesAsAService) unmarshaller.unmarshal(parameters.getFile());
			LOGGER.debug("Read");
			minutesAsAService = result;
		}  catch (JAXBException | SAXException ex) {
			throw new IOException(ex);
		}
	}
	
	/** Writes the config to the filesystem. 
	 * @see #read() 
	 */
	private void write() throws IOException {
		try {
			
			if (parameters.isBackup()) {
				backup();
			}
			Marshaller marshaller = jaxbc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(minutesAsAService, parameters.getFile());
		}  catch (JAXBException ex) {
			throw new IOException(ex);
		}
	}

	public final static DateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH'-'mm'-'ss");
	public final static String BACKUP_FOLDER_NAME = "backup";
	
	/** Does the backup of the file. */
	private void backup() {
		File f = parameters.getFile();
		File parent = f.getParentFile();
		File backupFolder = new File(parent, BACKUP_FOLDER_NAME);
		backupFolder.mkdirs();
		String backupName = BACKUP_DATE_FORMAT.format(new Date());
		File backup = new File(backupFolder, backupName);
		LOGGER.debug("Backing up {} to {}", f.getAbsolutePath(), backup.getAbsolutePath());
		f.renameTo(backup);
	}
	
	/** The main function. */
	public void run() throws IOException {
		try {
			// read from file
			read();
			
			Objects.requireNonNull(minutesAsAService);
			
			boolean accept;
			Optional<AttendeeType> choice = null;
			Set<String> attendeeIds = getEffectiveAttendeeIds();
			do {
				// choice using heavy magic
				choice = schedulingLogic(attendeeIds);

				accept = ui.askAccept(choice.get());
			} while (!accept);
				
			processChoice(choice.get());

			write();
		} catch (DatatypeConfigurationException ex) {
			throw new IOException(ex);
		}
	}
	
	/** Does the necessary alterations to the XML after chosing one attendee.
	 * @param mmaas the XML to alter.
	 * @param choice the attendee being the lucky winner.
	 */
	private void processChoice(AttendeeType choice) throws DatatypeConfigurationException {
		ObjectFactory factory = new ObjectFactory();
		LogType log = factory.createLogType();

		DatatypeFactory typeFactory = DatatypeFactory.newInstance();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		XMLGregorianCalendar xmlcal = typeFactory.newXMLGregorianCalendar(cal);
		log.setWhen(xmlcal);
		log.setAttendeeRef(choice.getId());
		minutesAsAService.getHistoryLogs().getLog().add(log);
		
		Map<String, Long> histo = calculateHistogram();
		Long oldCount = histo.get(choice.getId());
		Long newCount = oldCount + 1;
		choice.setCount(BigInteger.valueOf(newCount));
		histo.put(choice.getId(), newCount);
		
		updateAttendeeCountFromHistogram(histo, minutesAsAService.getPossibleAttendees().getAttendee());
	}
		
	private Set<String> getEffectiveAttendeeIds() throws IOException {
		Set<String> possibleAttendeeIds = MeetingMinutesAsAServices.listPossibleAttendeeIds(minutesAsAService);
		LOGGER.debug("possible attendees: {}", possibleAttendeeIds);
		Set<String> currentAttendeeIds = ui.knockOutAwayAttendeeIds(possibleAttendeeIds, minutesAsAService);
		LOGGER.debug("current attendees: {}", currentAttendeeIds);
		return currentAttendeeIds;
	}
	
	/** Does the scheduling logic. */
	private Optional<AttendeeType> schedulingLogic(Set<String> currentAttendeeIds) {
		Map<String, Long> histo = calculateHistogram();
		
		currentAttendeeIds.stream().filter(id -> !histo.containsKey(id)).forEach(id -> histo.put(id,0l));
			 
		Set<String> currentMinimalists = minimum(histo, currentAttendeeIds);
		String choice = choice(currentMinimalists);
		return MeetingMinutesAsAServices.findById(choice, minutesAsAService);
	}
	
	/** Chooses one entry of the given set with an equal probability.
	 */
	private String choice(Set<String> ids) {
		List<String> list = new ArrayList<>(ids);
		LOGGER.debug("Choice set: {}", list);
		int index = random.nextInt(list.size());
		LOGGER.debug("Random index: {}", index);
		return list.get(index);
	}

	/** Choses the attendees with the minimum of done meeting minutes.
	 * Note: Does NOT find attendees without any meeting minutes at all.
	 * @param histogram  the histogram from {@link #calculateHistogramFromLogs(de.einsundeins.mmaas.xml.MeetingMinutesAsAService) }.
	 * @param ids the possible ids available.
	 * @return the set of minimum meeting minute writers.
	 */
	private static Set<String> minimum(Map<String, Long> histogram, Set<String> ids) {
		Set<String> result;
		
		OptionalLong minimum = 
				histogram.entrySet().stream().filter(entry -> ids.contains(entry.getKey())).
				mapToLong(e -> e.getValue()).min();
		result = histogram.entrySet().stream().filter(entry -> ids.contains(entry.getKey())).
				filter(entry -> entry.getValue().equals(minimum.getAsLong())).
				map(entry -> entry.getKey()).
				collect(Collectors.toSet());
		
		LOGGER.debug("Minimum: {}, Result: {}", minimum, result);
		return result;
	}
	
	/** Updates {@link AttendeeType#count} fields from the given histogram. 
	 * @param histo the histogram that countains the count entries, for example
	 * from the method {@link #calculateHistogramFromLogs(de.einsundeins.mmaas.xml.MeetingMinutesAsAService) }.
	 * @param attendees the list of attendees to update the counts on.
	 */
	private static void updateAttendeeCountFromHistogram(Map<String, Long> histo, Collection<AttendeeType> attendees) {
		attendees.forEach(att -> att.setCount(BigInteger.valueOf(histo.getOrDefault(att.getId(), 0l))));
	}
	
	/** Calculates the histogram either from the logs or from the
	 * attendee counts. Falls back to the logs if either
	 * reindex option is given or one attendee count is {@code null}.
	 */
	private Map<String, Long> calculateHistogram() {
		Map<String, Long> histo;
		try {
			if (parameters.isReindex()) {
				histo = calculateHistogramFromLogs();
			} else {
				histo = calculateHistogramFromAttendeeCounts();
			}
		}
		catch (IllegalStateException e) {
			System.err.println(e.getMessage()+", falling back to counting logs");
			LOGGER.warn(e.getMessage(), e);
			histo = calculateHistogramFromLogs();			
		}
		return histo;
	}
	
	/** Calculates a histogram of meeting minute writers. 
	 * @return the histogram object with attendee ids being the keys and the
	 * counts being the values.
	 * @see #calculateHistogramFromAttendeeCounts(de.einsundeins.mmaas.xml.MeetingMinutesAsAService) 
	 */
	private Map<String, Long> calculateHistogramFromLogs() {
		Map<String, Long> result = minutesAsAService.getHistoryLogs().getLog().stream().
				collect(Collectors.groupingBy((LogType s) -> s.getAttendeeRef(), Collectors.counting()));
				
		LOGGER.debug("Histogram: {}", result);
		return result;
	}
	
	/** Gets the histogram from the attendee counts. 
	 * @return the histogram object with attendee ids being the keys and the
	 * counts being the values.
	 * @throws IllegalStateException if there is at least one attendee without count values.
	 * @see #calculateHistogramFromLogs(de.einsundeins.mmaas.xml.MeetingMinutesAsAService) 
	 */
	private Map<String, Long> calculateHistogramFromAttendeeCounts() {
		Map<String, Long> result;
		
		Optional<AttendeeType> nullAttendee = 
				minutesAsAService.getPossibleAttendees().getAttendee().stream().
						filter(att -> att.getCount() == null).findFirst();
		
		if (nullAttendee.isPresent()) {
			throw new IllegalStateException("No count for "+nullAttendee.get());
		}		
		result = minutesAsAService.getPossibleAttendees().getAttendee().stream().
				collect(Collectors.toMap(AttendeeType::getId, att -> att.getCount().longValue()));
		LOGGER.debug("Histogram: {}", result);
		return result;
	}
	
	public static void main(String args[]) throws CmdLineException, IOException, JAXBException {
		Parameters params = Parameters.parse(args);
		if (params == null) {
			return;
		}
		App app = new App(params);
		app.run();
	}
}
