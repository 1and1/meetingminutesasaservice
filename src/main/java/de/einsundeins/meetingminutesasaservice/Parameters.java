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

import java.io.File;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;

/**
 * Command line parsed parameters.
 * @author stephan
 */
public class Parameters {
	
	@Option(name = "-file", aliases = {"-f"}, required = true, usage = "Database XML config file", metaVar = "FILE")
	private File file;
		
	@Option(name = "-help", aliases = {"-h"}, required = false, usage = "Show this help")
	private boolean help;
	
	@Option(name = "-backup", aliases = {"-B"}, required = false, usage = "Do backups of the config", handler = ExplicitBooleanOptionHandler.class)
	private boolean backup = true;
	
	@Option(name = "-reindex", aliases = {"-I"}, required = false, usage = "Calculate index from meeting log entries")
	private boolean reindex;
	
	private Parameters() {
	}
	
	public static Parameters parse(String args[])  {
		Parameters result = new Parameters();
		CmdLineParser parser = new CmdLineParser(result);
		try {
			parser.parseArgument(args);
			
			if (result.help) {
				parser.printUsage(System.err);
				return null;
			}
				
		}
		catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return null;
		}
		return result;
	}

	public File getFile() {
		return file;
	}

	public boolean isBackup() {
		return backup;
	}

	public boolean isReindex() {
		return reindex;
	}
}
