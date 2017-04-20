package batch;

/**
 * http://www.cnblogs.com/panfeng412/archive/2012/06/19/storm-common-patterns-of-batching.html
 * Created by noah on 17-4-20.
 */

import java.sql.SQLException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.sql.Connection;
import java.sql.Statement;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class BatchingBolt implements IRichBolt {
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private Queue<Tuple> tupleQueue = new ConcurrentLinkedQueue<Tuple>();
	private int count;
	private long lastTime;
	private Connection conn;

	public BatchingBolt(int n) {
		count = n; //批量处理的Tuple记录条数
		//conn = DBManger.getConnection(); //通过DBManager获取数据库连接
		lastTime = System.currentTimeMillis(); //上次批量处理的时间戳
	}

	@Override
	public void prepare(Map stormConf, TopologyContext context,
	                    OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple tuple) {
		tupleQueue.add(tuple);
		long currentTime = System.currentTimeMillis();
		// 每count条tuple批量提交一次，或者每个1秒钟提交一次
		try {
			if (tupleQueue.size() >= count || currentTime >= lastTime + 1000) {
				Statement stmt = conn.createStatement();
				conn.setAutoCommit(false);
				for (int i = 0; i < count; i++) {
					Tuple tup = (Tuple) tupleQueue.poll();
					String sql = "";
					//String sql = DBManager.getSQL(tup); //生成sql语句
					stmt.addBatch(sql); //加入sql
					collector.ack(tup); //进行ack
				}
				stmt.executeBatch(); //批量提交sql

				conn.commit();
				conn.setAutoCommit(true);

				System.out.println("batch insert data into database, total records: " + count);
				lastTime = currentTime;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}