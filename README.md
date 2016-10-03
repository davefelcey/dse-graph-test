# Instructions for performing a simple unit test against DSE Graph
  
First of all you need to download and install DSE. See [here](https://academy.datastax.com/downloads) for details: 

Then navigate to the <DSE home>/bin directory and start DSE with the 
Graph services enabled (the -g flag enables Graph services and 
the -f flag runs it in the foreground)

  ```
  dse cassandra -g-f
  ```
         
To run the JUnit test use Maven as follows;

  ```
  mvn test
  ```
The case insensitive search test involves overriding the index created
by the graph index. This is done by doing the following;

1. Run the test the first time to create the Solr index. The test will 
fail the first time.

2. Fetch the schema and configuration for the Solr core created by
adding the graph index as follwos;

  ```
  bin/dsetool get_core_config remote_test.test_vertex_p > init_config.xml
  bin/dsetool get_core_schema remote_test.test_vertex_p > init_schema.xml
  cp init_schema.xml new_schema.xml
  ```
  
The last command creates a copy of the Solr core schema to update

3. Change the schema configuration of the field you want to be able to
perform case insensitive searches against, in this case the 'name' field,
adding the the 'text_general' filed type and modifying the type of the 
field;

  ```xml
  <!-- Add to field definitions -->
<fieldType name="text_general" class="solr.TextField" positionIncrementGap="100">
  <analyzer type="index">
    <tokenizer class="solr.StandardTokenizerFactory"/>
    <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
    <!-- in this example, we will only use synonyms at query time
    <filter class="solr.SynonymFilterFactory" synonyms="index_synonyms.txt" ignoreCase="true" expand="false"/>
    -->
    <filter class="solr.LowerCaseFilterFactory"/>
  </analyzer>
  <analyzer type="query">
    <tokenizer class="solr.StandardTokenizerFactory"/>
    <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
    <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true"/>
    <filter class="solr.LowerCaseFilterFactory"/>
  </analyzer>
</fieldType>

  <!-- Updated the name filed type from TextField to text_general -->
  <field docValues="true" indexed="true" multiValued="false" name="name" stored="true" type="text_general"/>
  ```

4. Upload the index resources required by the new field type as follows;

  ```
  bin/dsetool write_resource remote_test.test_vertex_p name=stopwords.txt file=stopwords.txt
  bin/dsetool write_resource remote_test.test_vertex_p name=synonyms.txt file=synonyms.txt
  ```
  
5. Finally rebuild and re-index the Solr core for the graph index as follows;

  ```
  bin/dsetool reload_core remote_test.test_vertex_p reindex=true schema=new_schema.xml solrconfig=init_config.xml
  ```

Note: if you add or modify the graph index at the moment there is a risk that 
the external modification will be overritten, but this will be addressed
in a future patch.