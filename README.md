# Meeting Minutes As A Service

Choses a meeting minute writer from a set of writers
defined in an XML configuration file.

## Operations

### Prerequisites

Prerequisites is Java 8 for running it and [Maven](https://maven.apache.org/) for building it.
It can run on Linux and Windows. One start wrapper script (mmaas.sh) works
on Linux. On Windows it only works with Cygwin (mmaas.bat).

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
    
#### XML Configuration documentation

An example of the configuration can be found [here](https://raw.githubusercontent.com/1and1/meetingminutesasaservice/master/src/main/resources/config.xml.template).

##### Element possibleAttendees

The element contains the set of possible meeting minute writers.

##### Element attendee

The element defines a single meeting minute writer. There are the following attributes:

* id: An unique identifier that will be referenced in the XML file. Can't be changed (String).
* name: A human readable identifier to be displayed for the attendee (String).
* enabled: Whether this entry shall be displayed as a possible meeting minute writer at all (boolean).
* count: The count of already written meeting minutes. This counter is used for maintaining the fairness to get an equally distributed meeting minute writing rate. If you need to add a new meeting minute writer, you need to put an appropriate start value here (integer).

#### Element historyLogs

Contains a log of who wrote when the meeting minutes. This list is only read when you call the command with the reindex parameter.

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

### License

The software is licensed under the [Apache license 2.0](http://www.apache.org/licenses/LICENSE-2.0).
