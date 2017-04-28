package sinova;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import sinova.util.PropertiesUtil;
import storm.kafka.*;

import java.util.Arrays;

/**
 * sinova.LoadDataTopology
 * kafkaSport-->blot-->file
 * Created by noah on 17-4-21.
 */
public class LoadDataTopology {
	public static final String KAFKA_SPOUT_ID = "kafkaSpout";
	public static final String TOPOLOGY_NAME = "TestTopology";

	public static void main(String[] args) {
		Config config = new Config();
		// 读取配置文件
		PropertiesUtil util = new PropertiesUtil("/conf.properties");
		config.put("file.name", util.getProperty("file.name"));
		config.put("local.path", util.getProperty("local.path"));// local file
		config.put("ftp.port", util.getProperty("ftp.port"));
		config.put("ftp.passwd", util.getProperty("ftp.passwd"));
		config.put("ftp.host", util.getProperty("ftp.host"));
		config.put("ftp.user", util.getProperty("ftp.user"));
		config.put("ftp.path", util.getProperty("ftp.path"));
		config.put("ftp.port", util.getProperty("ftp.port"));

		//kafka broker
		BrokerHosts brokerHosts = new ZkHosts(util.getProperty("kafka.zkHosts"), util.getProperty("kafka.brokers"));
		// broker,topic,zk_root,kafka_id
		SpoutConfig spoutConf = new SpoutConfig(brokerHosts,
				util.getProperty("kafka.topic"),
				util.getProperty("storm.zkRoot"),
				util.getProperty("storm.topic.id"));
		//设置如何处理kafka消息队列输入流
		spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
		spoutConf.forceFromStart = false;
		spoutConf.startOffsetTime = 0;
		// storm zk conf
		spoutConf.zkServers = Arrays.asList(util.getProperty("storm.zkServers").split(","));
		spoutConf.zkPort = Integer.valueOf(util.getProperty("storm.zkPort"));
		KafkaSpout spout = new KafkaSpout(spoutConf);
		// 创建topology
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(KAFKA_SPOUT_ID, spout, 3);
		builder.setBolt("executeBlot", new ExecuteBlot(), 1).shuffleGrouping(KAFKA_SPOUT_ID);
		//builder.setBolt("logBlot", new LogBlot(), 1).shuffleGrouping(KAFKA_SPOUT_ID);

		if (args.length == 0) {
			config.setNumWorkers(1);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
			waitForSeconds(1);
		} else if (args.length > 0) {
			try {
				config.setNumWorkers(Integer.valueOf(args[0]));
				config.setNumAckers(0);
				StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
			} catch (AlreadyAliveException e) {
				e.printStackTrace();
			} catch (InvalidTopologyException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage:" + TOPOLOGY_NAME + " [workers]");
		}
	}

	public static void waitForSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}
}
