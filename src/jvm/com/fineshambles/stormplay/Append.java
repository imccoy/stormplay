package com.fineshambles.stormplay;

import java.util.ArrayList;
import java.util.List;

import storm.trident.operation.CombinerAggregator;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Fields;

public class Append<T> implements CombinerAggregator<List<T>> {
	
	private int _fieldIndex;
	
	public Append(Fields fields, String name) {
		this(fields.fieldIndex(name));
	}
	
	public Append(int fieldIndex) {
		this._fieldIndex = fieldIndex;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> init(TridentTuple tuple) {
//		System.err.println("Append init tuple (" + tuple.size() + "):");
//		for (int i = 0; i < tuple.size(); i++) {
//			System.err.println("" + i + ": " + tuple.get(i).toString());
//		}
		ArrayList<T> l = new ArrayList<T>();
		if (tuple.size() != 0) {
			l.add((T)tuple.get(_fieldIndex));	
		}
		return l;
	}

	@Override
	public List<T> combine(List<T> val1, List<T> val2) {
		ArrayList<T> l = new ArrayList<T>();
		l.addAll(val1);
		l.addAll(val2);
		return l;
	}

	@Override
	public List<T> zero() {
		return new ArrayList<T>();
	}

}
