# Meeting Minutes As A Service

Choses a meeting minute writer from a set of writers
defined in an XML configuration file.

## Operations

### Prerequisites

Prerequisites is Java 8 for running it and Maven for building it.
It can run on Linux and Windows. The start wrapper script works
on Windows only with Cygwin.

### Building

Build it with Maven

    MeetingMinutesAsAService$ mvn clean package
    
### Installing

After building you have a tar ball in your target directory. You
can copy and extract this archive whereever you want.

### Configuring

The software comes with a template configuration file named
config.xml.template located in the config directory.
You can edit this file and later check it
using XML Schema, for example

    $ xmllint --noout --schema mmaas.xsd config.xml
    config.xml.template validates
    $

### Running

The software is started with the Shell wrapper script "mmaas.sh":

    $ bin/mmaas.sh -f config.xml 

Where config.xml is the config file. 
Note that the config file will get updated with the current
winners.

## Development

There's a github repository where the development is done

    https://github.com/1and1/meetingminutesasaservice
    
Feel free to report issues and post pull requests.

