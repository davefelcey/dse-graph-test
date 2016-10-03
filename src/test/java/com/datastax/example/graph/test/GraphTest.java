package com.datastax.example.graph.test;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Simple JUnit DSE Graph test example
 *
 * Created by davidfelcey on 30/09/2016.
 */
public class GraphTest {
    private static final Logger logger = LoggerFactory.getLogger(GraphTest.class);
    private static final String GRAPH_HOST = "127.0.0.1";
    private static final String GRAPH_NAME = "remote_test";
    private DseCluster dseCluster = null;
    private DseSession dseSession = null;

    /**
     * Connect to the DSE cluster,  establish a session and create a test graph
     *
     * @throws Exception if any errors occur
     */
    @org.junit.Before
    public void setUp() throws Exception {
        // Connect to DSE cluster
        dseCluster = DseCluster.builder()
                .addContactPoint(GRAPH_HOST)
                .build();
        dseSession = dseCluster.connect();
        logger.debug("Connected to " + GRAPH_HOST);

        // Create test graph
        dseSession.executeGraph("system.graph('" + GRAPH_NAME + "').ifNotExists().create()");
        logger.debug("Created graph " + GRAPH_NAME);
    }

    /**
     * Destroy the test graph and close the session with the DSE cluster
     *
     * @throws Exception if any errors occur
     */
    @org.junit.After
    public void tearDown() throws Exception {
        if (dseCluster != null) {
            // dseSession.executeGraph("system.graph('" + GRAPH_NAME + "').drop()");
            dseCluster.close();
            logger.debug("Disconnected from " + GRAPH_HOST);
        }
    }

    /**
     * Simple test to write and then read a vertex
     *
     * @throws Exception if any errors occour
     */
    @org.junit.Test
    public void testGraphQuery() throws Exception {
        try {
            // Insert a new vertex
            GraphStatement s1 = new SimpleGraphStatement("g.addV(label, 'test_vertex')").setGraphName(GRAPH_NAME);
            dseSession.executeGraph(s1);
            logger.debug("Added vertex to graph " + GRAPH_NAME);

            // Allow for graph modification
            TimeUnit.SECONDS.sleep(10);

            // Query the graph for the new vertex
            GraphStatement s2 = new SimpleGraphStatement("g.V()").setGraphName(GRAPH_NAME);
            GraphResultSet rs = dseSession.executeGraph(s2);
            String vertexLabel = rs.one().asVertex().getLabel();

            logger.debug("Vertex label: " + vertexLabel);

            // Check vertex label matches value created
            assertEquals(vertexLabel, "test_vertex");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}