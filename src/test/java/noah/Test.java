package noah;

/**
 * Test
 * Created by noah on 17-4-25.
 */
public class Test {

	@org.junit.Test
	public void testString() {
		String test = "test,a";
		System.out.println(test.substring(0,test.indexOf(",")));
	}

}
