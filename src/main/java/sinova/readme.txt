需求：每分钟同步短厅明细至省份FTP
文件名：	省id_yyyymmddhhmi_服务器名.log
	     如：010_201610281404_bj-yz302-c02-r720-3.log
方式：	strom
周期：	每分钟
服务器：
 	bj-yz302-f13-r720-3	10.70.11.16
	bj-yz302-f13-r720-4	10.70.11.17  >>>>	10.20.33.12
	bj-yz302-c02-r720-1	10.70.11.18
	bj-yz302-c02-r720-2	10.70.11.19
	bj-yz302-c02-r720-3	10.70.11.20
查看：http://10.70.11.16:8888/index.html
JAVA:   /sms_monitor/src/main/java/com/sinova/sms_monitor/topology/ShortHallServiceTop
代码：10.70.11.16 /app/sinova/unsm/sms_monitor

同步：/app/sinova/unsm/smslogs/ftp.sh
启动:
/app/sinova/apache-storm-0.9.5/bin/storm jar  /app/sinova/unsm/sms_monitor-0.0.1-SNAPSHOT.jar  com.sinova.sms_monitor.topology.ShortHallServiceTopology  1  1 1
svn文档的地址：http://192.168.101.48:19999/ltzb/svn/018.订单中心

充值交费 topic: thirdservice
 flume 从kafak>>hive表 ： 10.70.51.11
thirdservicetohdfs.conf
kth.sources.source3.type = org.apache.flume.source.kafka.KafkaSource
kth.sources.source3.zookeeperConnect = 10.70.50.12:2181,10.70.50.11:2181,10.70.50.12:2181/kafka
kth.sources.source3.topic = thirdservice
kth.sources.source3.groupId = thirdserviceGroup2
kth.sources.source3.kafka.fetch.message.max.bytes = 1073741824
kth.sources.source3.batchSize = 10000
kth.sources.source3.batchDurationMillis = 1000
kth.sources.source3.kafka.auto.commit.enable = true
kth.sources.source3.kafka.consumer.timeout.ms =1000
------------------------------------------------------------------------
kafka topic查看:
bin/kafka-topics.sh --list \
--zookeeper 10.70.50.12:2181,10.70.50.11:2181,10.70.48.11:2181/kafka
数据:
bin/kafka-console-consumer.sh \
--zookeeper 10.70.50.12:2181,10.70.50.11:2181,10.70.48.11:2181/kafka \
--topic thirdservice \
--from-beginning

	20170424101403628219||20170424101403||android@5.0||30029||银行卡充值缴费||17681229942||030||301||11||2||113000005||20||10.20.34.66||domain_client1_node110||1||||||||||||||||||||
	20170424101426258619||20170424101426||android@5.2||30029||银行卡充值缴费||17693293718||087||870||11||2||113000005||23||10.20.34.66||domain_client1_node110||1||||||||||||||||||||
	20170424101432390224||20170424101432||iphone_c@5.2||30029||银行卡充值缴费||18645530806||097||989||02||2||113000004||25||10.20.34.66||domain_client1_node110||1||||||||||||||||||||
	20170424101444848155||20170424101444||iphone_c@5.2||30029||银行卡充值缴费||13093766658||036||360||01||2||113000004||34||10.20.34.66||domain_client1_node110||1||||||||||||||||||||
	20170424101451395232||20170424101451||android@5.2||30029||银行卡充值缴费||13251239113||083||831||01||1||113000005||25||10.20.34.66||domain_client1_node110||1||||||||||||||||||||
	20170424101453942128||20170424101453||android@5.0||30029||银行卡充值缴费||17681229942||030||301||11||2||113000005||30||10.20.34.66||domain_client1_node110||1||||||||||||||||||||

bin/kafka-topics.sh --describe \
--zookeeper 10.70.50.12:2181,10.70.50.11:2181,10.70.48.11:2181/kafka \
--topic thirdservice

	Topic:thirdservice	PartitionCount:3	ReplicationFactor:3	Configs:
		Topic: thirdservice	Partition: 0	Leader: 3	Replicas: 3,2,11	Isr: 3,2,11
		Topic: thirdservice	Partition: 1	Leader: 11	Replicas: 11,3,12	Isr: 3,12,11
		Topic: thirdservice	Partition: 2	Leader: 12	Replicas: 12,11,1	Isr: 1,12,11
