package ch04.backtransactions;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;
import java.util.Random;

public class RandomFailureBolt extends BaseRichBolt {

	private static final Integer MAX_PERCENT_FAIL = 80;
	Random random = new Random();
	private OutputCollector collector;

	@Override
	public void execute(Tuple input) {
		Integer r = random.nextInt(100);
		if (r > MAX_PERCENT_FAIL) {
			collector.ack(input);
		} else {
			collector.fail(input);
		}

	}

	@Override
	public void prepare(Map stormConf, TopologyContext context,
	                    OutputCollector collector) {
		this.collector = collector;
	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}


}