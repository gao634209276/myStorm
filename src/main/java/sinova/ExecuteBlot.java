package sinova;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
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
	private String local_path = null;
	private String ftp_host = null;
	private int ftp_port = 21;
	private String ftp_user = null;
	private String ftp_passwd = null;
	private String file_name = null;
	private String ftp_path = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		file_name = stormConf.get("file.name").toString();
		local_path = stormConf.get("local.path").toString();
		ftp_port = Integer.parseInt(stormConf.get("ftp.port").toString());
		ftp_passwd = stormConf.get("ftp.passwd").toString();
		ftp_host = stormConf.get("ftp.host").toString();
		ftp_user = stormConf.get("ftp.user").toString();
		ftp_path = stormConf.get("ftp.path").toString();
	}


	@Override
	public void execute(Tuple input) {
		if (input.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) && input.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)) {
			LOG.info("---------------RENAME File AND FTP-------------------");
			renameFile();
			ftpFile();
		} else {
			writeFile(input);
		}
		collector.ack(input);
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
	 * file.tmp-->file
	 */
	public void renameFile() {
		File dir = new File(local_path);
		File[] fileLists = null;
		if (dir.exists() && dir.isDirectory()) {
			fileLists = dir.listFiles();
		}
		if (null != fileLists) {
			for (File f : fileLists) {
				String tmp;
				if (f.isFile() && (tmp = f.getName()).endsWith(".tmp")) {
					String newName = tmp.substring(0, tmp.indexOf(".tmp"));
					File toFile = new File(local_path + File.separator + newName);
					boolean b = f.renameTo(toFile);
				}
			}
		}
	}

	/**
	 * mv local_path/file --> /ftp_path/MOB-UPAY-MIN
	 */
	private void ftpFile() {
		FTPClient ftpClient = new FTPClient();
		InputStream fis = null;
		try {
			ftpClient.connect(ftp_host, ftp_port);
			ftpClient.login(ftp_user, ftp_passwd);
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				ftpClient.disconnect();
				LOG.error("FTP CLIENT LOGIN FAIL !!! ");
				return;
			}
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.changeWorkingDirectory(ftp_path);

			File dir = new File(local_path);
			File[] fileLists = null;
			if (dir.exists() && dir.isDirectory()) {
				fileLists = dir.listFiles();
			}
			if (null != fileLists) {
				for (File localFile : fileLists) {
					if (localFile.isFile() && !localFile.getName().endsWith(".tmp")) {
						fis = new FileInputStream(localFile);
						ftpClient.storeFile(file_name, fis);
						IOUtils.closeQuietly(fis);
						boolean delete = localFile.delete();
						LOG.info("DELETE FILE : " + localFile.getName() + (delete ? " SUCCESS " : " FAIL "));
					}
				}
			}
		} catch (IOException e) {
			LOG.error("FTP FAIL : ", e);
		} finally {
			try {
				if (!ftpClient.isConnected()) {
					ftpClient.logout();
				}
				IOUtils.closeQuietly(fis);
				ftpClient.disconnect();
			} catch (IOException e1) {
				LOG.error("FTP DESTROY FAIL : ", e1);
			}
		}
	}

	private void writeFile(Tuple tuple) {
		String content = tuple.getString(0).replace("||", ",");
		String filName = file_name + ".tmp";
		File file = new File(local_path + filName);
		// 写文件
		FileWriterWithEncoding writer = null;
		try {
			if (!file.exists()) {
				boolean isSuccess = file.createNewFile();
				LOG.info("CREATE NEW FILE : " + (isSuccess ? "SUCCESS" : "FAIL"));
			}
			writer = new FileWriterWithEncoding(file.getAbsoluteFile(), "utf-8", true);
			writer.write(content);
			writer.write("\r\n");// 写入换行
		} catch (IOException e) {
			LOG.error(" WRITE FILE FAIL !!!" + e);
		} finally {
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
				LOG.error(" WRITE FILE DESTROY FAIL !!!" + e);
				//e.printStackTrace();
			}
		}
	}
}
