# Meeting Minutes As A Service

Choses a meeting minute writer from a set of writers
defined in an XML configuration file.

## Operations

### Installation

Install one of the Debian / Fedora packages from the [releases](https://github.com/1and1/meetingminutesasaservice/releases)
section.

After that, you have a new command installed that is called "meetingminutesasaservice".
    
### Configuring

The software comes with a template configuration file named
config.xml.template installed in the directory "/usr/share/doc/meetingminutesasaservice/".
You can copy and edit this file and later check it
using XML Schema, for example

    $ xmllint --noout --schema mmaas.xsd config.xml
    config.xml.template validates
    $

### Running

The software is started with the Shell wrapper script "meetingminutesasaservice":

    $ meetingminutesasaservice -f config.xml 

Where config.xml is the config file. 
Note that the config file will get updated with the current
winners.
    
### XML Configuration documentation

An example of the configuration can be found [here](https://raw.githubusercontent.com/1and1/meetingminutesasaservice/master/src/main/resources/config.xml.template).

#### Element possibleAttendees

The element contains the set of possible meeting minute writers.

#### Element attendee

The element defines a single meeting minute writer. There are the following attributes:

* id: An unique identifier that will be referenced in the XML file. Can't be changed (String).
* name: A human readable identifier to be displayed for the attendee (String).
* enabled: Whether this entry shall be displayed as a possible meeting minute writer at all (boolean).
* count: The count of already written meeting minutes. This counter is used for maintaining the fairness to get an equally distributed meeting minute writing rate. If you need to add a new meeting minute writer, you need to put an appropriate start value here (integer).

#### Element historyLogs

Contains a log of who wrote when the meeting minutes. This list is only read when you call the command with the reindex parameter.

## Development

There's a github repository where the development is done

    https://github.com/1and1/meetingminutesasaservice
    
Feel free to report issues and post pull requests.

### Building

Build it with Maven

    MeetingMinutesAsAService$ mvn clean package

### License

The software is licensed under the [Apache license 2.0](http://www.apache.org/licenses/LICENSE-2.0).
