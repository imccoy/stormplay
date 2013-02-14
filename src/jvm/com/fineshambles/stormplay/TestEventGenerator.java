package com.fineshambles.stormplay;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;

public class TestEventGenerator {
	public static final String DEFINITION_FIELD = "definition";
	public static final String WORD_FIELD = "word";
	public static final String TIME_FIELD = "time";

	private int maxCount = 2;
	private String[] words = new String[] { "Dog" , "Cat", "Mouse", "Tiger" };
	private String[] attributes = new String[] { "fierce", "petite", "monstrous", "adorable" };
	private String[] analogies = new String[] { "dragon", "unicorn", "centaur", "phoenix" };
	
	private LinkedBlockingQueue<ArrayList<Object>>[] queues;

	public TestEventGenerator(int n) {
		queues = new LinkedBlockingQueue[n];
		for (int i = 0; i < n; i++) {
			queues[i] = new LinkedBlockingQueue<ArrayList<Object>>(128);
		}
		fill();
	}
	
	void fill() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int count = 0;
				while (true) {
					count += 1;
					if (count > maxCount){
						addTuple(new ArrayList<Object>());
						return;
					}
					ArrayList<Object> tuple = newTuple(count);
					addTuple(tuple);
				}
			}

			public void addTuple(ArrayList<Object> tuple) {
				int n = 0;
				for (LinkedBlockingQueue<ArrayList<Object>> q : queues) {
					while (true) {
						try {
							q.put(tuple);
							break;
						} catch (InterruptedException e) {
							System.err.println("interrupted while putting, retrying");
							e.printStackTrace();
						}
					}
				}
			}

			public ArrayList<Object> newTuple(int count) {
				ArrayList<Object> tuple = new ArrayList<Object>();
				String word = pickArrayElem(words);
				String definition = "Like a " + pickArrayElem(analogies) +
						               " but more " + pickArrayElem(attributes);
				tuple.add(count);
				tuple.add(word);
				tuple.add(definition);
				return tuple;
			} }).start();
	}


	public IRichSpout get(int n) {
		return new TestEventSpout(n, queues[n]);
	}
	
	private static class TestEventSpout extends BaseRichSpout {
		
		private SpoutOutputCollector collector;
		private LinkedBlockingQueue<ArrayList<Object>> queue;
		private boolean done = false;
		private int n;
	
	
		public TestEventSpout(int n,
				LinkedBlockingQueue<ArrayList<Object>> linkedBlockingQueue) {
			this.n = n;
			this.queue = linkedBlockingQueue;
		}

		@Override
		public void open(Map conf, TopologyContext context,
				SpoutOutputCollector collector) {
			this.collector = collector;
		}
	
		@Override
		public void nextTuple() {
			if (done)
				return;
			ArrayList<Object> tuple = getNext();
			if (tuple.size() == 0) {
				done = true;
				return;
			}
			System.out.println("EMIT " + n + " " + tuple.get(0).toString());
			collector.emit(tuple);
		}

		public ArrayList<Object> getNext() {
			while (true) {
				try {
					return queue.take();
				} catch (InterruptedException e) {
					System.err.println("interrupted while taking, retrying");
					e.printStackTrace();
				}
			}
		}

	
		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(getFields());
		}
	}
	
	private<T> T pickArrayElem(T[] list) {
		int idx = (int)(Math.random() * list.length);
		return list[idx];
	}

	public static Fields getFields() {
		return new Fields(TIME_FIELD, WORD_FIELD, DEFINITION_FIELD);
	}
	
}
