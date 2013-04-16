rdb2graph - Relational Database To Property Graph Converter
===========================================================

*rdb2graph can be used to convert a relational database on-line into a property graph model and store it in a graph database*

The tool uses [Apache DdlUtils](http://db.apache.org/ddlutils/) to read the schema of a relational database. Based on table definitions and 
foreign key constraints, nodes and relationships are created. Link tables (n:m) have to be explicitly declared in a config file if they shall 
be converted into a single relation. Properties (table attributes) and some additional meta-data (origin, type, id) are stored at the nodes 
and relationships.

Currently, the tool supports MySQL as a source database system and Neo4j as a target system. The architecture is flexible and can be easily
extended by implementing some predefined interfaces.

Usage
-----

Config: 

* copy resources/sample-config.xml to resources/config.xml and look inside for details

Install: 

Microsoft SQL JDBC Driver (in case you haven't installed)

* download from [Microsoft](http://msdn.microsoft.com/en-us/sqlserver/aa937724.aspx)
* unpack sqljdbc4.jar
* `mvn install:install-file -Dfile=sqljdbc4.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0 -Dpackaging=jar`

rdb2graph

* `mvn install`

Run: 

* `mvn exec:java` to see the available options
* `mvn exec:java -Dexec.args="-c config.xml"` to apply your own config xml (default is resources/config.xml)
* `mvn exec:java -Dexec.args="-e ddl.xml"` to extract the database schema into ddl.xml
* `mvn exec:java -Dexec.args="-t"` to transform the relational database form source system to target system