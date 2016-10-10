package wc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

public class ReportBolt extends BaseRichBolt{
	private static final long serialVersionUID = 1L;
	private Map<String, Long> counts;

	/**
	 * It provides the bolt with the environment in which the bolt executes. This includes the:
	 * 该方法在component在集群的worker上初始化的时候调用一次,
	 * 其提供bolt执行所需要的环境
	 * @param stormConf  该bolt的storm配置,他会和该bolt对应的配置合并
	 * @param context  可以用于获取关于这个task在topology的位置信息,包括该task的task id以及component id,input,output信息等
	 * @param collector 这个collector用于从spout中进行发出(emit)元祖(tuple);所有的tuple将被随时发出,包括prepare和cleanup方法,
	 *                  该方法是线程安全的,讲会保存为bolt对象的一个变量实例
	 */
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.counts = new HashMap<String,Long>();
	}
	// 声明输出格式
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}

	public void execute(Tuple input) {
		String word = input.getStringByField("word");
		Long count = input.getLongByField("count");
		counts.put(word, count);
	}

	/**
	 * 当一个bolt关闭的时候调用
	 * 由于supervisor上使用kill -9 关掉集群的work,cleanup讲不会被调用
	 * 当运行为local模式的时候,当一个topology被killed,cleanup会保证被调用
	 */
	public void cleanup() {
		System.out.println("Final output");
		Iterator<Entry<String, Long>> iter = counts.entrySet().iterator();
		while (iter.hasNext()) {
		    Entry<String, Long> entry = iter.next();
		    String word = (String) entry.getKey();
		    Long count = (Long) entry.getValue();
		    System.out.println(word + " : " + count);
		} 
		
		super.cleanup();
	}	
	
}