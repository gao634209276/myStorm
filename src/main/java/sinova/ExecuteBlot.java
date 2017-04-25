package sinova;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Write File
 * 每分钟同步短厅明细至省份FTP
 * Created by noah on 17-4-21.
 */
public class ExecuteBlot extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(ExecuteBlot.class);
	private OutputCollector collector;
	private String path = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		path = stormConf.get("sms_log_path").toString();
	}

	@Override
	public void execute(Tuple input) {
		if (input.getSourceComponent()
				.equals(Constants.SYSTEM_COMPONENT_ID) &&
				input.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)) {
			LOG.info("rename File and FTP :");
			renameFile();
		} else {
			writeFile(input);
			collector.ack(input);
		}
	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 60);
		return conf;
	}

	/**
	 * mv /path/MOB-UPAY-MIN.tmp /path/MOB-UPAY-MIN
	 */
	private void renameFile() {
		File file = new File(path);
		// 判断文件目录是否存在，且是文件目录，非文件
		if (file.exists() && file.isDirectory()) {
			File[] childFiles = file.listFiles();
			String path = file.getAbsolutePath();
			if (null != childFiles) {
				for (File childFile : childFiles) {
					// 如果是文件
					if (childFile.isFile()) {
						String oldName = childFile.getName();
						// 判断后缀名是否是tmp
						if (oldName.contains(".tmp")) {
							String suffix = oldName.substring(oldName.indexOf("."));
							if (suffix.equalsIgnoreCase(".tmp")) {
								String newName = oldName.substring(0, oldName.indexOf("."));
								boolean b = childFile.renameTo(new File(path + File.separator + newName));
							}
						}
					}
				}
			}
		}
	}

	private void writeFile(Tuple tuple) {
		String content = tuple.getString(0).replace("||", ",");
		String filName = "MOB-UPAY-MIN.tmp";
		File file = new File(path + filName);
		// 写文件
		FileWriterWithEncoding writer = null;
		try {
			if (!file.exists()) {
				boolean b = file.createNewFile();
			}
			writer = new FileWriterWithEncoding(file.getAbsoluteFile(), "utf-8", true);
			writer.write(content);
			writer.write("\r\n");// 写入换行
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Description: 向FTP服务器上传文件
	 *
	 * @param url      FTP服务器hostname
	 * @param port     FTP服务器端口
	 * @param username FTP登录账号
	 * @param password FTP登录密码
	 * @param path     FTP服务器保存目录
	 * @param filename 上传到FTP服务器上的文件名
	 * @param input    输入流
	 * @return 成功返回true，否则返回false
	 * http://blog.csdn.net/hbcui1984/article/details/2720204
	 */
	public static boolean uploadFile(String url, int port, String username, String password, String path, String filename, InputStream input) {
		boolean success = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(url, port);//连接FTP服务器
			//如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
			ftp.login(username, password);//登录
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return success;
			}
			ftp.changeWorkingDirectory(path);
			ftp.storeFile(filename, input);

			input.close();
			ftp.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					//
				}
			}
		}
		return success;
	}
}
