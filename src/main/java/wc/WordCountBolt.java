package wc;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
//See Storm分布式实时计算模式 Page8
//BaseBasicBolt 直接继承 BaseComponent (IComponent) 和实现 IBasicBolt (IComponent)
//BaseRichBolt 继承 BaseComponent (IComponent)和实现 IRichBolt (IBolt, IComponent)

public class WordCountBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	// HashMap是可序列化的,在构造函数中实例化也是安全的
	// 通常情况下最好在构造函数中对基本数据类型和可序列化对象复制和实例化
	// 在prepare方法中对不可序列化的对象中进行实例化
	private Map<String, Long> counts = null;

	// prepare在IBolt中定义,在bolt初始化时调用
	// 用来准备bolt用到的资源,如数据链接等
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		this.counts = new HashMap<String, Long>();
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word", "count"));
	}

	public void execute(Tuple input) {
		String word = input.getStringByField("word");
		Long count = this.counts.get(word);
		if (count == null) {
			count = 0L;
		}
		count++;
		this.counts.put(word, count);
		this.collector.emit(new Values(word, count));
		// System.out.println(count);
	}

}