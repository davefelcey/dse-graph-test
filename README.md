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
by the graph index. This can be done as follows;

1. Run the test the first time to create the Solr index. The test will 
fail the first time because you have not made the search index case 
sensitive yet.

2. Fetch the schema and configuration for the Solr core created by
adding the graph index and then copy the schema so it can be
updated as follwos;

  ```
  $DSE_HOME/bin/dsetool get_core_config remote_test.test_vertex_p > init_config.xml
  $DSE_HOME/bin/dsetool get_core_schema remote_test.test_vertex_p > init_schema.xml
  cp init_schema.xml new_schema.xml
  ```

3. Change the schema configuration of the field you want to be able to
perform case insensitive searches against, in this case the 'name' field. To do this
you need to add a new field type, 'text_general' and modify the type of the 'name' field definition 
to be 'text_general'. The format of the new field type and updated 'name' field are shown below.

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

4. Upload the index resources (contained in this example) required by the new field type as follows;

  ```
  $DSE_HOME/bin/dsetool write_resource remote_test.test_vertex_p name=stopwords.txt file=stopwords.txt
  $DSE_HOME/bin/dsetool write_resource remote_test.test_vertex_p name=synonyms.txt file=synonyms.txt
  ```
  
5. Finally rebuild and re-index the Solr core for the graph index as follows;

  ```
  $DSE_HOME/bin/dsetool reload_core remote_test.test_vertex_p reindex=true schema=new_schema.xml solrconfig=init_config.xml
  ```
