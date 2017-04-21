package sinova.util;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * properties 资源文件解析工具
 */
public class PropertiesUtil {

	private Properties props;

	public PropertiesUtil(String fileName) {
		readProperties(fileName);
	}

	private void readProperties(String fileName) {
		try {
			props = new Properties();
			InputStream fis = getClass().getResourceAsStream(fileName);
			props.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取某个属性
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * 获取所有属性，返回一个map,不常用
	 * 可以试试props.putAll(t)
	 */
	public Map getAllProperty() {
		Map map = new HashMap();
		Enumeration enu = props.propertyNames();
		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();
			String value = props.getProperty(key);
			map.put(key, value);
		}
		return map;
	}

	/**
	 * 根据属性获取map
	 */
	public Map getPropertyMap(String key) {
		Map map = new HashMap();
		String content = getProperty(key);

		String[] contentArr = content.split(";");
		for (int i = 0; i < contentArr.length; i++) {
			String[] type = contentArr[i].split(",");
			map.put(type[0], type[1]);
		}
		return map;
	}

	/**
	 * 在控制台上打印出所有属性，调试时用。
	 */
	public void printProperties() {
		props.list(System.out);
	}

	public static void main(String[] args) {
		PropertiesUtil util = new PropertiesUtil("/conf.properties");
		util.getPropertyMap("netType");
	}
}
