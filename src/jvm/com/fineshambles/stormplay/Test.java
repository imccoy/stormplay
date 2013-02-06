package com.fineshambles.stormplay;

import backtype.storm.Config;

import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * This is a basic example of a Storm topology.
 */
public class Test {
    
    public static void main(String[] args) throws Exception {
    	DefinitionsRepository.asplosion();
    	
        TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout("events", new TestEventSpout(), 2);        
        builder.setBolt("groupedDefinitions", new WordAddedBolt(), 3)
                .shuffleGrouping("events");
        builder.setBolt("words", new DefinitionsBolt(), 2)
                .fieldsGrouping("groupedDefinitions", new Fields("word"));
        builder.setBolt("wordsPersister", new DefinitionsPersistBolt(), 2)
                .shuffleGrouping("words");
                
        Config conf = new Config();
        conf.setDebug(true);
        
        if(args!=null && args.length > 0) {
            conf.setNumWorkers(3);
            
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(5000);
            cluster.killTopology("test");
            cluster.shutdown();
            
            startWebServer();
        }
    }

	private static void startWebServer() {
		
		ResourceConfig rc = new PackagesResourceConfig("com.fineshambles.stormplay.resources");
		URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9995).build();
		try {
			GrizzlyServerFactory.create(BASE_URI, rc).listen();
			Utils.sleep(60000);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}