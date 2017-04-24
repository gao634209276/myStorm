package sinova;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Write File
 * 每分钟同步短厅明细至省份FTP
 * Created by noah on 17-4-21.
 */
public class WritreBlot extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(WritreBlot.class);
	private OutputCollector collector;
	private String path = null;
	private String yyyyMMddHHmm = null;
	private String address = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		DateTime dateTime = new DateTime();
		yyyyMMddHHmm = dateTime.toString("yyyyMMddHHmm");
		path = stormConf.get("sms_log_path").toString();
		try {
			address = InetAddress.getLocalHost().getHostName();
			//threadname=Thread.currentThread().getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(Tuple input) {
		if (input.getSourceComponent()
				.equals(Constants.SYSTEM_COMPONENT_ID) &&
				input.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)) {
			renameFile();
			DateTime dateTime = new DateTime();
			yyyyMMddHHmm = dateTime.toString("yyyyMMddHHmm");
		}
		if (input.contains("fields")) {
			writeFile(input);
		}
		//collector.emit(new Values(input));
		collector.ack(input);
	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		//declarer.declare(new Fields("provinceId", "content"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		//Map<String, Object> conf = new HashMap<String, Object>();
		Config conf = new Config();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 60);
		return conf;
	}

	private void renameFile() {
		File file = new File(path);
		// 判断文件目录是否存在，且是文件目录，非文件
		if (file.exists() && file.isDirectory()) {
			File[] childFiles = file.listFiles();
			String path = file.getAbsolutePath();
			for (File childFile : childFiles) {
				// 如果是文件
				if (childFile.isFile()) {
					String oldName = childFile.getName();
					if (oldName.contains(".tmp")) {
						// 判断后缀名是否是tmp
						String suffix = oldName.substring(oldName.indexOf("."));
						if (suffix.equalsIgnoreCase(".tmp")) {
							String newName = oldName.substring(0, oldName.indexOf(".")) + ".log";
							childFile.renameTo(new File(path + File.separator + newName));
						}
					}
				}
			}
		}
	}

	private void writeFile(Tuple tuple) {
		String content = tuple.getStringByField("content");
		String provinceId = tuple.getStringByField("provinceId");
		String filName = provinceId + "_" + yyyyMMddHHmm + "_" + address + ".tmp";
		File file = new File(path + filName);
		// 写文件
		FileWriterWithEncoding writer = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriterWithEncoding(file.getAbsoluteFile(), "utf-8", true);
			writer.write(content);
			writer.write("\r\n");// 写入换行
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
