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
 * kafkaSport-->blot-->file
 * Created by noah on 17-4-21.
 */
public class LoadDataTopology {
	public static final String KAFKA_SPOUT_ID = "kafka_smsbusTopic";
	public static final String TOPOLOGY_NAME = "shortHallServiceTopology";
	private static final String KAFKA_TOPIC = "smsbusTopic";
	private static final String KAFKA_ID = "smsbusTopicId";

	public static void main(String[] args) {
		Config config = new Config();
		// 读取配置文件
		PropertiesUtil util = new PropertiesUtil("/conf.properties");
		BrokerHosts brokerHosts = new ZkHosts(util.getProperty("zks"));
		SpoutConfig spoutConf = new SpoutConfig(brokerHosts, KAFKA_TOPIC, util.getProperty("zkRoot"), KAFKA_ID);
		spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
		spoutConf.forceFromStart = false;
		spoutConf.startOffsetTime = 0;
		String zkServers = util.getProperty("zkServers");
		config.put("sms_log_path", util.getProperty("sms_log_path"));
		String zkSers[] = new String[3];
		for (int i = 0; i < 3; i++) {
			zkSers[i] = zkServers.split(",")[i];
		}
		spoutConf.zkServers = Arrays.asList(zkSers);
		spoutConf.zkPort = Integer.valueOf(util.getProperty("zkPort"));
		KafkaSpout spout = new KafkaSpout(spoutConf);
		// 创建topology
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(KAFKA_SPOUT_ID, spout, 5);
		//builder.setBolt("shortHallServiceBolt", new ShortHallServiceBolt(),5).shuffleGrouping(KAFKA_SPOUT_ID);
		//builder.setBolt("shortHallServiceWriteFileBolt",new ShortHallServiceWriteFileBolt(),1).shuffleGrouping("shortHallServiceBolt");
		if (args.length == 0) {
			config.setNumWorkers(1);
			LocalCluster cluster = new LocalCluster();

			cluster.submitTopology(TOPOLOGY_NAME, config,
					builder.createTopology());
			waitForSeconds(1);
		} else if (args.length == 3) {
			try {
				config.setNumWorkers(Integer.valueOf(args[0]));
				config.setNumAckers(0);
				StormSubmitter.submitTopology(TOPOLOGY_NAME, config,
						builder.createTopology());
			} catch (AlreadyAliveException e) {
				e.printStackTrace();
			} catch (InvalidTopologyException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage:" + TOPOLOGY_NAME
					+ " [workers] [spouts] [bolts]");
		}
	}


	public static void waitForSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}

}
