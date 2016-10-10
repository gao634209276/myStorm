package ch02;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * 负责得到并标准化每行文本。它把文本行切分成单词，大写转化成小写，去掉头尾空白符。
 */
public class WordNormalizer extends BaseRichBolt {
	private OutputCollector collector;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {

		this.collector = collector;
	}


	/**
	 * 每次接收到元组时都会被调用一次，还会再发布若干个元组。
	 *
	 * *bolt*从单词文件接收到文本行，并标准化它。
	 * <p>
	 * 文本行会全部转化成小写，并切分它，从中得到所有单词。
	 */
	public void execute(Tuple input) {
		String sentence = input.getString(0);

		String[] words = sentence.split(" ");
		for (String word : words) {
			word = word.trim();
			if (!word.isEmpty()) {
				word = word.toLowerCase();
				//发布这个单词
				collector.emit(new Values(word));
			}
		}
		//对元组做出应答
		collector.ack(input);
	}


	/**
	 * 首先我们要声明bolt的出参：这里我们声明bolt将发布一个名为“word”的域。
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word"));
	}

}
