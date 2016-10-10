package wc;

import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

public class SentenceSpout extends BaseRichSpout {
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector collector;
	private int index = 0;
	private String[] sentences = { "when i was young i'd listen to the radio",
			"waiting for my favorite songs", "when they played i'd sing along",
			"it make me smile",
			"those were such happy times and not so long ago",
			"how i wondered where they'd gone",
			"but they're back again just like a long lost friend",
			"all the songs i love so well", "every shalala every wo'wo",
			"still shines.", "every shing-a-ling-a-ling",
			"that they're starting", "to sing so fine"};


	/**
	 * open方法在component的一个task在集群的worker上被初始化时候被调用
	 * 它提供了spout的执行环境,
	 * @param conf 该spout在Storm中的配置,该conf会和集群在该机器上的配置合并
	 * @param context 可以用于获取关于这个task在topology的位置信息,包括该task的task id以及component id,input,output信息等
	 * @param collector 这个collector用于从spout中进行发出(emit)元祖(tuple);所有的tuple将被立即发出,包括open,close方法,
	 *                  该collector是线程安全的,会被保存为一个spout对象的实例变量
	 */
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
	}


	/**
	 * 声明topology中所有流的输出schema(格式)
	 * @param declarer 用于定义输出流的id,字段,是否是一个直接流
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("sentence"));
	}

	/**
	 * 当这个方法别调用时候,storm将请求spout把tuple发出到collector
	 * 这个方法不会阻塞,因此如果spout没有tuple发出,该方法将返回
	 * nextTuple,ack,fail会在spout任务的一个单线程中被快速的循环调用,
	 * 在没有tuple发出的时候,让线程休眠1ms是优雅的,这将不会浪费太多的cpu资源
	 */
	public void nextTuple() {
		Utils.sleep(500);
		this.collector.emit(new Values(sentences[index]));
		index++;
		if (index >= sentences.length) {
			index = 0;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
}