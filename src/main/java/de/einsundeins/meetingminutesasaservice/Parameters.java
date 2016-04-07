/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
