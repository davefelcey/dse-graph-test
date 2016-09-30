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
