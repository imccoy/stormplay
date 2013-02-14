//package com.fineshambles.stormplay;
//
//import storm.trident.testing.FixedBatchSpout;
//import backtype.storm.Config;
//import backtype.storm.LocalCluster;
//import backtype.storm.LocalDRPC;
//import backtype.storm.StormSubmitter;
//import backtype.storm.generated.StormTopology;
//import backtype.storm.tuple.Fields;
//import backtype.storm.tuple.Values;
//import storm.trident.TridentState;
//import storm.trident.TridentTopology;
//import storm.trident.operation.BaseFunction;
//import storm.trident.operation.CombinerAggregator;
//import storm.trident.operation.TridentCollector;
//import storm.trident.operation.builtin.Count;
//import storm.trident.operation.builtin.FilterNull;
//import storm.trident.operation.builtin.MapGet;
//import storm.trident.operation.builtin.Sum;
//import storm.trident.planner.processor.StateQueryProcessor;
//import storm.trident.testing.MemoryMapState;
//import storm.trident.tuple.TridentTuple;
//
//
//public class Test {    
//    public static class Split extends BaseFunction {
//        @Override
//        public void execute(TridentTuple tuple, TridentCollector collector) {
//            String sentence = tuple.getString(0);
//            collector.emit(new Values(sentence.split(" ")[0], sentence));
//        }
//    }
//    
//    public static class Echo extends BaseFunction {
//        @Override
//        public void execute(TridentTuple tuple, TridentCollector collector) {
//        	String msg = "got tuple (";
//        	if (tuple == null) {
//        		msg += "null";
//        	} else {
//	        	for (int i = 0; i < tuple.size(); i++) {
//	        		msg += tuple.get(i);
//	        		if (i != tuple.size() - 1)
//	        			msg += ", ";
//	        	}
//        	}
//            System.out.println(tuple + ")");
//            collector.emit(tuple.getValues());
//        }
//    }
//
//    
//    public static StormTopology buildTopology(LocalDRPC drpc) {
//        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3,
//                new Values("the cow jumped over the moon"),
//                new Values("the man went to the store and bought some candy"),
//                new Values("four score and seven years ago"),
//                new Values("how many apples can you eat"),
//                new Values("to be or not to be the person"));
//        spout.setCycle(true);
//        
//        TridentTopology topology = new TridentTopology();        
//        TridentState wordCounts =
//              topology.newStream("spout1", spout)
//                .parallelismHint(16)
//                .each(new Fields("sentence"), new Split(), new Fields("word", "definition"))
//                .groupBy(new Fields("word"))
//                .persistentAggregate(new MemoryMapState.Factory(), new Fields("definition"),
//                                     new Append(0), new Fields("count"))         
//                .parallelismHint(16);
//                
//        topology.newDRPCStream("words", drpc)
////                .each(new Fields("args"), new Split(), new Fields("word"))
////                .groupBy(new Fields("word"))
//                .stateQuery(wordCounts, new Fields("args"), new MapGet(), new Fields("count"))
//                ;
//        return topology.build();
//    }
//    
//    public static void main(String[] args) throws Exception {
//        Config conf = new Config();
//        conf.setMaxSpoutPending(20);
//        if(args.length==0) {
//            LocalDRPC drpc = new LocalDRPC();
//            LocalCluster cluster = new LocalCluster();
//            cluster.submitTopology("wordCounter", conf, buildTopology(drpc));
//            for(int i=0; i<100; i++) {
//                System.out.println("DRPC RESULT: " + drpc.execute("words", "the"));
//                Thread.sleep(1000);
//            }
//        } else {
//            conf.setNumWorkers(3);
//            StormSubmitter.submitTopology(args[0], conf, buildTopology(null));        
//        }
//    }
//}


package com.fineshambles.stormplay;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.MapGet;
import storm.trident.testing.MemoryMapState;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.StormSubmitter;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import clojure.lang.Compiler;



public class Test {
    
	public static class AddConst extends BaseFunction {
		private Object _cnst;

		public AddConst(Object cnst) {
			this._cnst = cnst;
		}

		@Override
		public void execute(TridentTuple tuple, TridentCollector collector) {
			List<Object> list = new ArrayList<Object>();
			list.addAll(tuple.getValues());
			list.add(_cnst);
			collector.emit(new Values(list.toArray()));
		}
	}
	
    public static void main(String[] args) throws Exception {
        TridentTopology topology = new TridentTopology();
        
		LocalDRPC drpc = new LocalDRPC();

        TestEventGenerator generator = new TestEventGenerator(2);
        
        TridentState definitions = topology.newStream("definitions", generator.get(0))
        						     .groupBy(new Fields(TestEventSpout.WORD_FIELD))
                                     .persistentAggregate(
						        		  new MemoryMapState.Factory(),
						        		  new Fields(TestEventSpout.DEFINITION_FIELD),
						        		  new Append<String>(0),
						        		  new Fields("definitions"));
        TridentState recentWords = topology.newStream("words", generator.get(1))
					        		.each(new Fields("word"), new AddConst("w"), new Fields("word0", "const"))
					        		.groupBy(new Fields("const"))
					                .persistentAggregate(
						        		  new MemoryMapState.Factory(),
						        		  new Fields("word0"),
						        		  new Append<String>(0),
						        		  new Fields("words"));
        
        topology.newDRPCStream("definitions-for", drpc)
        		.stateQuery(definitions, new Fields("args"), new MapGet(), new Fields("definitions"));
        topology.newDRPCStream("recent-words", drpc)
        		.stateQuery(recentWords, new Fields("args"), new MapGet(), new Fields("words"));
        
        DefinitionsRepository.connect(drpc);
        
                
        Config conf = new Config();
        conf.setDebug(true);
        conf.setMaxSpoutPending(20);
        
        if(args!=null && args.length > 0) {
            conf.setNumWorkers(3);
            
            StormSubmitter.submitTopology(args[0], conf, topology.build());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, topology.build());
            
            Compiler.load(new StringReader("(ns user (:require [com.fineshambles.stormplay.web :as web])) (web/start)"));
            
            cluster.killTopology("test");
            cluster.shutdown();
            
            
        }
    }

}
