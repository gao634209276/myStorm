package tick;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * MyTickTestTopology
 * Created by noah on 17-4-21.
 */
public class MyTickTestTopology {


	public static class WordCount extends BaseBasicBolt {
		Map<String, Integer> counts = new HashMap<String, Integer>();

		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			if (tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
					&& tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)) {
				System.out.println("################################WorldCount bolt: "
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
			} else {
				collector.emit(new Values("a", 1));
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word", "count"));
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			Config conf = new Config();
			conf.put(conf.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 10);
			return conf;
		}
	}

	public static class TickTest extends BaseBasicBolt {
		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			// 收到的tuple是tick tuple
			if (tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
					&& tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)) {
				System.out.println("################################TickTest bolt: "
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
			}
			// 收到的tuple时正常的tuple
			else {
				collector.emit(new Values("a"));
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("test"));
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			Config conf = new Config();
			conf.put(conf.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 20);
			return conf;
		}
	}

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout", new RandomSentenceSpout(), 3);
		builder.setBolt("count", new WordCount(), 3).shuffleGrouping("spout");
		builder.setBolt("tickTest", new TickTest(), 3).shuffleGrouping("count");
		Config conf = new Config();
		conf.put(conf.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 7);
		conf.setDebug(false);
		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
		} else {
			conf.setMaxTaskParallelism(3);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("word-count", conf, builder.createTopology());
			// Thread.sleep(10000);
			// cluster.shutdown();
		}
	}

	private static class RandomSentenceSpout implements IRichSpout {
		@Override
		public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

		}

		@Override
		public void close() {

		}

		@Override
		public void activate() {

		}

		@Override
		public void deactivate() {

		}

		@Override
		public void nextTuple() {

		}

		@Override
		public void ack(Object msgId) {

		}

		@Override
		public void fail(Object msgId) {

		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {

		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			return null;
		}
	}
}