package ch02;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

/**
 * text-->line
 */
public class WordReader extends BaseRichSpout {

	private SpoutOutputCollector collector;
	private FileReader fileReader;
	private boolean completed = false;

	/**
	 * 第一个被调用
	 * 我们将创建一个文件并维持一个collector对象
	 *
	 * @param conf 配置对象，在定义topology对象是创建
	 * @param context TopologyContext对象，包含所有拓扑数据
	 * @param collector 还有SpoutOutputCollector对象，它能让我们发布交给bolts处理的数据
	 */
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		try {
			//创建了一个FileReader对象，用来读取文件
			this.fileReader = new FileReader(conf.get("wordsFile").toString());
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error reading file [" + conf.get("wordFile") + "]");
		}
		this.collector = collector;
	}

	/**
	 * 这个方法做的惟一一件事情就是分发文件中的文本行
	 */
	@Override
	public void nextTuple() {
		/**
		 *  这个方法会不断的被调用，直到整个文件都读完了，我们将等待并返回。
		 */
		if (completed) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//什么也不做
			}
			return;
		}
		String str;
		//创建reader
		BufferedReader reader = new BufferedReader(fileReader);
		try {
			//读所有文本行
			while ((str = reader.readLine()) != null) {
				/**
				 * 按行发布一个新值
				 */
				this.collector.emit(new Values(str), str);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading tuple", e);
		} finally {
			completed = true;
		}
	}


	@Override
	public void ack(Object msgId) {
		System.out.println("OK:" + msgId);
	}

	@Override
	public void close() {
	}

	@Override
	public void fail(Object msgId) {
		System.out.println("FAIL:" + msgId);
	}



	/**
	 * 声明输入域"word"
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("line"));
	}
}
