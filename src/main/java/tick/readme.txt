 关于Storm tick
1. tick的功能
	Apache Storm中内置了一种定时机制——tick，
		它能够让任何bolt的所有task每隔一段时间（精确到秒级，用户可以自定义）收到一个来自__systemd的__tick stream的tick tuple，
		bolt收到这样的tuple后可以根据业务需求完成相应的处理。
	Tick功能从Apache Storm 0.8.0版本开始支持，本文在Apache Storm 0.9.1上测试。

2. 在代码中使用tick及其作用
	在代码中如需使用tick，可以参照下面的方式：

2.1. 为bolt设置tick
	若希望某个bolt每隔一段时间做一些操作，那么可以将bolt继承BaseBasicBolt/BaseRichBolt，
	并重写getComponentConfiguration()方法。在方法中设置Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS的值，单位是秒。
	getComponentConfiguration()是backtype.storm.topology.IComponent接口中定义的方法，
	在此方法的实现中可以定义以 ”Topology.*” 开头的此 bolt 特定的 Config 。
		@Override
		public Map<String, Object> getComponentConfiguration() {
		    Config conf = new Config();
		    conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 60);
		    return conf;
		}
	这样设置之后，此bolt的所有task都会每隔一段时间收到一个来自__systemd的__tick stream的tick tuple，
	因此execute()方法可以实现如下：
	public void execute(Tuple tuple) {
		// 收到tuple是tick tuple
		if(tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
		&& tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)){
			//do something ...
		}
		// 收到tuple是正常的tuple
		else{
			//do something ...
		}
	}
2.2. 为Topology全局设置tick
	若希望Topology中的每个bolt都每隔一段时间做一些操作，那么可以定义一个Topology全局的tick，
	同样是设置Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS的值：
	Config conf = new Config();
	conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 10);
	StormSubmitter.submitTopology(args[0],conf,builder.createTopology();

2.3. tick设置的优先级
	与Linux中的环境变量的优先级类似，storm中的tick也有优先级，即全局tick的作用域是全局bolt，
	但对每个bolt其优先级低于此bolt定义的tick。
	这个参数的名字TOPOLOGY_TICK_TUPLE_FREQ_SECS具有一定的迷惑性，一眼看上去应该是Topology全局的，但实际上每个bolt也可以自己定义。

2.4. tick的精确度
	Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS是精确到秒级的。
	例如某bolt设置Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS为10s，理论上说bolt的每个task应该每个10s收到一个tick tuple。
	实际测试发现，这个时间间隔的精确性是很高的，一般延迟（而不是提前）时间在 1ms 左右。

3. storm tick的实现原理
	在bolt中的getComponentConfiguration()定义了该bolt的特定的配置后，
	storm框架会在TopologyBuilder.setBolt()方法中调用bolt的getComponentConfiguration()方法，从而设置该bolt的配置。
	调用路径为：
		TopologyBuilder.setBolt()
		-> TopologyBuilder.initCommon()
		-> getComponentConfiguration()
4. 附件
测试使用的代码：See
	http://www.cnblogs.com/kqdongnanf/p/4778672.html?utm_source=tuicool&utm_medium=referral